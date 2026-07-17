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
    private final Replicator replicator;

    public HttpServer(Database db, ShardRouter router, Replicator replicator) {
        this.db = db;
        this.router = router;
        this.replicator = replicator;
        this.server = Javalin.create(config -> {
            config.requestLogger.http((ctx, ms) ->
            log.debug("{} {} -> {} ({} ms)", ctx.method(), ctx.path(), ctx.status(), ms));
            config.routes.get("/get/{key}", this::handleGet);
            config.routes.post("/set/{key}", this::handleSet);
            config.routes.post("/internal/replicate/{key}", this::handleReplicate);
        });
    }

    public void start(int port) {
        server.start(port);
    }

    private void handleGet(Context ctx) {
        String key = ctx.pathParam("key");
        if (!router.isPrimary(key)){ 
            redirectToPrimary(ctx, key); 
            return;
        }
        byte[] value = db.getKey(key);
        if (value == null) {
            ctx.status(404).result("Key not found");
            return;
        } 
        ctx.result(value);
    }

    private void handleSet(Context ctx) {
        String key = ctx.pathParam("key");
        if (!router.isPrimary(key)){ 
            redirectToPrimary(ctx, key); 
            return;
        }
        byte[] value = ctx.bodyAsBytes();
        db.setKey(key, value);
        replicator.replicate(router.replicaAddresses(key), key, value);
        ctx.status(201);
    }

    private void redirectToPrimary(Context ctx, String key) {
        String url = "http://" + router.primaryAddress(key) + ctx.path();
        ctx.redirect(url, HttpStatus.TEMPORARY_REDIRECT);
    }

    private void handleReplicate(Context ctx) {
        db.setKey(ctx.pathParam("key"), ctx.bodyAsBytes());
        ctx.status(201);
    }

    Javalin javalin() {
        return server;
    }

    @Override
    public void close() {
        server.stop();
    }
}
