package io.github.michalnowowiejski.distributedkv.network;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.github.michalnowowiejski.distributedkv.storage.Database;

public class HttpServer implements AutoCloseable {
    
    private final Database db;
    private final Javalin server;

    public HttpServer(Database db) {
        this.db = db;
        this.server = Javalin.create(config -> {
            config.routes.get("/get/{key}", this::handleGet);
            config.routes.post("/set/{key}", this::handleSet);
        });
    }

    public void start(int port) {
        server.start(port);
    }

    private void handleGet(Context ctx) {
        String key = ctx.pathParam("key");
        byte[] value = db.getKey(key);
        if (value == null) {
            ctx.status(404).result("Key not found");
            return;
        } 
        ctx.result(value);
    }

    private void handleSet(Context ctx) {
        String key = ctx.pathParam("key");
        byte[] value = ctx.bodyAsBytes();
        db.setKey(key, value);
        ctx.status(201);
    }

    @Override
    public void close() {
        server.stop();
    }
}
