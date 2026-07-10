package io.github.michalnowowiejski.distributedkv;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import picocli.CommandLine;
import picocli.CommandLine.Option;

@CommandLine.Command(name = "distributed-kv", mixinStandardHelpOptions = true)

public class Main {

    @Option(names = "--db-location", description = "The path to the RocksDB database", required = true)
    private String dbLocation;

    public static void main(String[] args) throws Exception {

        Main app = new Main();
        new CommandLine(app).parseArgs(args);

        RocksDB.loadLibrary();

        try (final Options options = new Options().setCreateIfMissing(true);
             final RocksDB db = RocksDB.open(options, app.dbLocation)) {
            db.put("a".getBytes(), "v".getBytes());
            final byte[] value = db.get("a".getBytes());
            System.out.println(new String(value));

        } catch (RocksDBException e) {
            System.err.println("Error opening database '" + app.dbLocation + "': " + e.getMessage());
        }

    }
}
