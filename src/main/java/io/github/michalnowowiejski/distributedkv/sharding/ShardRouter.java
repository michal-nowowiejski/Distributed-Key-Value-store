package io.github.michalnowowiejski.distributedkv.sharding;

public final class ShardRouter {

    private final HashRing ring;
    private final Shard self;

    public ShardRouter(HashRing ring, Shard self) {
        this.ring = ring;
        this.self = self;
    }

    public boolean isLocal(String key){
        return ring.shardForKey(key).equals(self);
    }

    public String ownerAddress(String key){
        Shard owner = ring.shardForKey(key);
        if (owner == null) {
            throw new IllegalArgumentException("No shard found for index: " + owner);
        }
        return owner.address();
    }
}
