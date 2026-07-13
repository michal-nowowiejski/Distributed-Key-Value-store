package io.github.michalnowowiejski.distributedkv;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import io.github.michalnowowiejski.distributedkv.network.HttpServer;
import io.github.michalnowowiejski.distributedkv.storage.Database;
import io.github.michalnowowiejski.distributedkv.config.Config;
import io.github.michalnowowiejski.distributedkv.config.ConfigLoader;

@CommandLine.Command(name = "distributed-kv", mixinStandardHelpOptions = true)
public class Main {

    @Option(names = "--db-location", description = "The path to the RocksDB database", required = true)
    private String dbLocation;

    @Option(names = "--port", description = "The port to run the server on", required = true)
    private int port;

    @Option(names = "--config", description = "Configuration file for static sharding")
    private String configFile = "sharding.yaml";

    public static void main(String[] args) {

        Main app = new Main();
        new CommandLine(app).parseArgs(args);

        Config config = ConfigLoader.load(app.configFile);
        System.out.println("Loaded configuration: " + config);

        Database db = Database.newDatabase(app.dbLocation);
        HttpServer server = new HttpServer(db);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.close();
            db.close();
        }));

        server.start(app.port);
        System.out.println("Listening on port " + app.port);
    }
}
