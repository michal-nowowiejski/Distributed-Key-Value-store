package io.github.michalnowowiejski.distributedkv.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.Response;
import io.github.michalnowowiejski.distributedkv.sharding.HashRing;
import io.github.michalnowowiejski.distributedkv.sharding.Shard;
import io.github.michalnowowiejski.distributedkv.sharding.ShardRouter;
import io.github.michalnowowiejski.distributedkv.storage.Database;

class HttpServerTest {

    private static final List<Shard> SHARDS = List.of(
        new Shard("shard0", 0, "localhost:8080"),
        new Shard("shard1", 1, "localhost:8081"));

    @Test
    void getMissingLocalKeyReturns404(@TempDir Path tempDir) {
        HashRing ring = new HashRing(SHARDS);
        try (Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            Shard self = ring.shardForKey("key");
            HttpServer server = new HttpServer(db, new ShardRouter(ring, self, 1), new Replicator());

            JavalinTest.test(server.javalin(), (app, client) -> {
                assertEquals(404, client.get("/get/key").code());
            });
        }
    }

    @Test
    void setLocalKeyStoresValueAndReturns201(@TempDir Path tempDir) {
        HashRing ring = new HashRing(SHARDS);
        try(Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            Shard self = ring.shardForKey("key");
            HttpServer server = new HttpServer(db, new ShardRouter(ring, self, 1), new Replicator());

            JavalinTest.test(server.javalin(), (app, client) -> {
                assertEquals(201, client.post("/set/key", "value").code());
                assertEquals("value", client.get("/get/key").body().string());
            });
        }
    }

    @Test
    void setRemoteKeyRedirectsWith307(@TempDir Path tempDir){
        HashRing ring = new HashRing(SHARDS);
        try(Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            Shard owner = ring.shardForKey("key");
            Shard self = SHARDS.stream()
                .filter(s -> !s.equals(owner))
                .findFirst()
                .orElseThrow();

            HttpServer server = new HttpServer(db, new ShardRouter(ring, self, 1), new Replicator());

            JavalinTest.test(server.javalin(), (app, client) -> {
                Response res = client.post("/set/key", "value");
                assertEquals(307, res.code());
                assertTrue(res.headers().get("Location").get(0).endsWith("set/key"));
            });
        }
    }
    
    @Test
    void writeToPrimaryPropagatesToReplica(@TempDir Path tempDir) throws Exception {
        HashRing ring = new HashRing(SHARDS);

        String key = "key";
        Shard primary = ring.shardForKey(key);
        Shard replica = SHARDS.stream().filter(s -> !s.equals(primary)).findFirst().orElseThrow();

        try (Database primaryDb = Database.newDatabase(tempDir.resolve("primary").toString());
            Database replicaDb = Database.newDatabase(tempDir.resolve("replica").toString());
            HttpServer primaryServer = new HttpServer(primaryDb, new ShardRouter(ring, primary, 2), new Replicator());
            HttpServer replicaServer = new HttpServer(replicaDb, new ShardRouter(ring, replica, 2), new Replicator())) {

            primaryServer.start(primary.port());
            replicaServer.start(replica.port());

            var res = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://" + primary.address() + "/set/" + key))
                    .POST(HttpRequest.BodyPublishers.ofString("value"))
                    .build(),
                HttpResponse.BodyHandlers.discarding());

            assertEquals(201, res.statusCode());
            assertArrayEquals("value".getBytes(StandardCharsets.UTF_8), replicaDb.getKey(key));
        }
    }
}
