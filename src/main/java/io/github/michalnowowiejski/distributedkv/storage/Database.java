package io.github.michalnowowiejski.distributedkv.storage;

import java.nio.charset.StandardCharsets;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class Database implements AutoCloseable {
    
    private final Options options;
    private final RocksDB db;

    private Database(Options options, RocksDB db) {
        this.options = options;
        this.db = db;
    }

    public static Database newDatabase(String path) {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        try {
            RocksDB db = RocksDB.open(options, path);
            return new Database(options, db);
        } catch (RocksDBException e) {
            options.close();
            throw new DatabaseException("Failed to open database at '" + path + "'", e);
        }
    }

    public void setKey(String key, byte[] value){
        try {
            db.put(key.getBytes(StandardCharsets.UTF_8), value);
        } catch (RocksDBException e) {
            throw new DatabaseException("Failed to set key", e);
        }
    }

    public byte[] getKey(String key){
        try {
            return db.get(key.getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            throw new DatabaseException("Failed to get key", e);
        }
    }

    @Override
    public void close() {
        db.close();
        options.close();
    }
    
}
