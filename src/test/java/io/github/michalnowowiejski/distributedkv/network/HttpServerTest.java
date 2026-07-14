package io.github.michalnowowiejski.distributedkv.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.Response;
import io.github.michalnowowiejski.distributedkv.sharding.Shard;
import io.github.michalnowowiejski.distributedkv.sharding.Sharder;
import io.github.michalnowowiejski.distributedkv.sharding.ShardRouter;
import io.github.michalnowowiejski.distributedkv.storage.Database;

class HttpServerTest {

    private static final List<Shard> SHARDS = List.of(
        new Shard("shard0", 0, "localhost:8080"),
        new Shard("shard1", 1, "localhost:8081"));

    @Test
    void getMissingLocalKeyReturns404(@TempDir Path tempDir) {
        Sharder sharder = new Sharder(2);
        try (Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            int myIdx = sharder.shardForKey("key");
            HttpServer server = new HttpServer(db, new ShardRouter(sharder, myIdx, SHARDS));

            JavalinTest.test(server.javalin(), (app, client) -> {
                assertEquals(404, client.get("/get/key").code());
            });
        }
    }

    @Test
    void setLocalKeyStoresValueAndReturns201(@TempDir Path tempDir) {
        Sharder sharder = new Sharder(2);
        try(Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            int myIdx = sharder.shardForKey("key");
            HttpServer server = new HttpServer(db, new ShardRouter(sharder, myIdx, SHARDS));

            JavalinTest.test(server.javalin(), (app, client) -> {
                assertEquals(201, client.post("/set/key", "value").code());
                assertEquals("value", client.get("/get/key").body().string());
            });
        }
    }

    @Test
    void setRemoteKeyRedirectsWith307(@TempDir Path tempDir){
        Sharder sharder = new Sharder(2);
        try(Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            int ownerIdx = sharder.shardForKey("key");
            int myIdx = 1 - ownerIdx;
            HttpServer server = new HttpServer(db, new ShardRouter(sharder, myIdx, SHARDS));

            JavalinTest.test(server.javalin(), (app, client) -> {
                Response res = client.post("/set/key", "value");
                assertEquals(307, res.code());
                assertTrue(res.headers().get("Location").get(0).endsWith("set/key"));
            });
        }

    }

}
