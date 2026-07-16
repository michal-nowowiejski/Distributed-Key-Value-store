package io.github.michalnowowiejski.distributedkv;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import io.github.michalnowowiejski.distributedkv.network.HttpServer;
import io.github.michalnowowiejski.distributedkv.storage.Database;
import io.github.michalnowowiejski.distributedkv.config.Config;
import io.github.michalnowowiejski.distributedkv.config.ConfigLoader;
import io.github.michalnowowiejski.distributedkv.sharding.HashRing;
import io.github.michalnowowiejski.distributedkv.sharding.Shard;
import io.github.michalnowowiejski.distributedkv.sharding.ShardRouter;

@CommandLine.Command(name = "distributed-kv", mixinStandardHelpOptions = true)
public class Main {

    @Option(names = "--db-location", description = "The path to the RocksDB database", required = true)
    private String dbLocation;

    @Option(names = "--config", description = "Configuration file for static sharding")
    private String configFile = "sharding.yaml";

    @Option(names = "--shard", description = "The name of the shard to run this instance on", required = true)
    private String shardName;

    public static void main(String[] args) {

        Main app = new Main();
        new CommandLine(app).parseArgs(args);

        Config config = ConfigLoader.load(app.configFile);
        System.out.println("Loaded configuration: " + config);
        
        Shard self = config.shards().stream()
            .filter(s -> s.name().equals(app.shardName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Shard not found: " + app.shardName));
        
        HashRing ring = new HashRing(config.shards());
        ShardRouter shardRouter = new ShardRouter(ring, self);
        Database db = Database.newDatabase(app.dbLocation);
        HttpServer server = new HttpServer(db, shardRouter);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.close();
            db.close();
        }));

        String address = self.address();
        int port = Integer.parseInt(address.substring(address.lastIndexOf(':') + 1));
        server.start(port);
        System.out.println("Listening on port " + port);
    }
}
