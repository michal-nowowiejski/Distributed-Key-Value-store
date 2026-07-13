package io.github.michalnowowiejski.distributedkv.sharding;

public class Sharder {
    
    private int shardCount;

    public Sharder(int shardCount) {
        this.shardCount = shardCount;
    }

    public int shardForKey(String key) {
        return Math.floorMod(fnv1a(key), shardCount);
    }

    private int fnv1a(String key){
        int hash = 0x811c9dc5;
        for (byte b : key.getBytes()) {
            hash ^= (b & 0xff);
            hash *= 0x01000193;
        }
        return hash;
    }
}
