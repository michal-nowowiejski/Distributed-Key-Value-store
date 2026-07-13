package io.github.michalnowowiejski.distributedkv.sharding;

import java.nio.charset.StandardCharsets;

public class Sharder {
    
    private int shardCount;

    public Sharder(int shardCount) {
        this.shardCount = shardCount;
        if (shardCount < 1) {
            throw new IllegalArgumentException("shardCount must be >= 1, was " + shardCount);
        }
    }

    public int shardForKey(String key) {
        return Math.floorMod(fnv1a(key), shardCount);
    }

    private int fnv1a(String key){
        int hash = 0x811c9dc5;
        for (byte b : key.getBytes(StandardCharsets.UTF_8)) {
            hash ^= (b & 0xff);
            hash *= 0x01000193;
        }
        return hash;
    }
}
