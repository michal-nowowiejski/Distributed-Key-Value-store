package io.github.michalnowowiejski.distributedkv;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import io.github.michalnowowiejski.distributedkv.storage.Database;
import io.github.michalnowowiejski.distributedkv.storage.DatabaseException;
import io.javalin.Javalin;

@CommandLine.Command(name = "distributed-kv", mixinStandardHelpOptions = true)
public class Main {

    @Option(names = "--db-location", description = "The path to the RocksDB database", required = true)
    private String dbLocation;

    @Option(names = "--port", description = "The port to run the server on", required = true)
    private int port;

    public static void main(String[] args) {

        Main app = new Main();
        new CommandLine(app).parseArgs(args);

        Database db = Database.newDatabase(app.dbLocation);

        var server = Javalin.create(config -> {
            config.routes.get("/get", ctx -> ctx.result("called get"));
            config.routes.post("/set", ctx -> ctx.result("called set"));
        }).start(app.port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
            db.close();
        }));

        System.out.println("Listening on port " + app.port);
    }
}
