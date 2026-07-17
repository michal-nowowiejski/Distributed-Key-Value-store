package io.github.michalnowowiejski.distributedkv.sharding;

import java.util.List;

public final class ShardRouter {

    private final HashRing ring;
    private final Shard self;
    private final int replicationFactor;

    public ShardRouter(HashRing ring, Shard self, int replicationFactor) {
        this.ring = ring;
        this.self = self;
        this.replicationFactor = replicationFactor;
    }

    public boolean isPrimary(String key){
        return preferenceList(key).get(0).equals(self);
    }

    private List<Shard> preferenceList(String key){
        return ring.shardsForKey(key, replicationFactor);
    }

    public String primaryAddress(String key){
        return preferenceList(key).get(0).address();
    }

    public List<String> replicaAddresses(String key){
        return preferenceList(key).stream()
            .filter(s -> !s.equals(self))
            .map(Shard::address)
            .toList();
    }
}
