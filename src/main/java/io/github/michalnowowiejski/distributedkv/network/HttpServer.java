package io.github.michalnowowiejski.distributedkv.network;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.github.michalnowowiejski.distributedkv.sharding.ShardRouter;
import io.github.michalnowowiejski.distributedkv.storage.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
    
    private final Database db;
    private final Javalin server;
    private final ShardRouter router;

    public HttpServer(Database db, ShardRouter router) {
        this.db = db;
        this.router = router;
        this.server = Javalin.create(config -> {
            config.requestLogger.http((ctx, ms) ->
            log.debug("{} {} -> {} ({} ms)", ctx.method(), ctx.path(), ctx.status(), ms));
            config.routes.get("/get/{key}", this::handleGet);
            config.routes.post("/set/{key}", this::handleSet);
        });
    }

    public void start(int port) {
        server.start(port);
    }

    private void handleGet(Context ctx) {
        String key = ctx.pathParam("key");
        if (router.isLocal(key)){
            byte[] value = db.getKey(key);
            if (value == null) {
                ctx.status(404).result("Key not found");
                return;
            } 
            ctx.result(value);
        } else {
            redirect(ctx, key);
        }   
    }

    private void handleSet(Context ctx) {
        String key = ctx.pathParam("key");
        if (router.isLocal(key)){
            byte[] value = ctx.bodyAsBytes();
            db.setKey(key, value);
            ctx.status(201);
        } else {
            redirect(ctx, key);
        }
    }

    private void redirect(Context ctx, String key) {
        String url = "http://" + router.ownerAddress(key) + ctx.path();
        ctx.redirect(url, HttpStatus.TEMPORARY_REDIRECT);
    }

    Javalin javalin() {
        return server;
    }

    @Override
    public void close() {
        server.stop();
    }
}
