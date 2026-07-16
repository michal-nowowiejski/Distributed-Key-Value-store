package io.github.michalnowowiejski.distributedkv.sharding;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class HashRing {
    
    private static final int vNODES = 150;
    private NavigableMap<Integer, Shard> ring = new TreeMap<>();

    public HashRing(List<Shard> shards){
        if (shards.isEmpty()) throw new IllegalArgumentException("ring needs at least one shard");
        for(Shard shard : shards){
            for (int i=0; i<vNODES; i++){
                ring.put(fnv1a(shard.name() + '#' + i), shard);
            }
        }
    }

    public Shard shardForKey(String key){
        int hash = fnv1a(key);
        var entry = ring.ceilingEntry(hash);
        if (entry == null) entry = ring.firstEntry();
        return entry.getValue();
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
