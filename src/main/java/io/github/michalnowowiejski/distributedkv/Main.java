package io.github.michalnowowiejski.distributedkv;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.michalnowowiejski.distributedkv.config.Config;
import io.github.michalnowowiejski.distributedkv.config.ConfigLoader;
import io.github.michalnowowiejski.distributedkv.network.HttpServer;
import io.github.michalnowowiejski.distributedkv.network.Replicator;
import io.github.michalnowowiejski.distributedkv.sharding.HashRing;
import io.github.michalnowowiejski.distributedkv.sharding.Shard;
import io.github.michalnowowiejski.distributedkv.sharding.ShardRouter;
import io.github.michalnowowiejski.distributedkv.storage.Database;

import picocli.CommandLine;
import picocli.CommandLine.Option;

@CommandLine.Command(name = "distributed-kv", mixinStandardHelpOptions = true)
public final class Main implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    @Option(names = "--db-location", description = "The path to the RocksDB database", required = true)
    private String dbLocation;

    @Option(names = "--config", description = "Configuration file for static sharding")
    private String configFile = "sharding.yaml";

    @Option(names = "--shard", description = "The name of the shard to run this instance on", required = true)
    private String shardName;

    @Override
    public Integer call() {
        Config config = ConfigLoader.load(configFile);
        Shard self = config.shardByName(shardName);
        log.info("Starting {} on {}", self.name(), self.address());

        HashRing ring = new HashRing(config.shards());
        
        //TODO retrieve replication factor from config file
        ShardRouter shardRouter = new ShardRouter(ring, self, 2);
        Database db = Database.newDatabase(dbLocation);
        Replicator replicator = new Replicator();
        HttpServer server = new HttpServer(db, shardRouter, replicator);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server...");
            server.close();
            db.close();
        }));
        
        server.start(self.port());

        return 0;
    }
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        if (exitCode != 0){
            System.exit(exitCode);
        }
    }
}
