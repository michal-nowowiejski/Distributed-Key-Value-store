package io.github.michalnowowiejski.distributedkv.sharding;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public final class HashRing {
    
    private static final int vNODES = 150;
    private final NavigableMap<Integer, Shard> ring = new TreeMap<>();
    private final int shardCount;

    public HashRing(List<Shard> shards){
        if (shards.isEmpty()) throw new IllegalArgumentException("ring needs at least one shard");
        for(Shard shard : shards){
            for (int i=0; i<vNODES; i++){
                ring.put(fnv1a(shard.name() + '#' + i), shard);
            }
        }
        this.shardCount = shards.size();
    }

    public Shard shardForKey(String key){
        return shardsForKey(key, 1).get(0);
    }

    public List<Shard> shardsForKey(String key, int count){
        if (count > shardCount){
            throw new IllegalArgumentException("count " + count + " exceeds shard count " + shardCount);
        }
        int hash = fnv1a(key);
        Set<Shard> owners = new LinkedHashSet<>();
        for (Shard s : ring.tailMap(hash, true).values()){
            if (owners.add(s) && owners.size() == count) return List.copyOf(owners);
        }
        for (Shard s : ring.headMap(hash, false).values()){
            if (owners.add(s) && owners.size() == count) break;
        }
        return List.copyOf(owners);
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
