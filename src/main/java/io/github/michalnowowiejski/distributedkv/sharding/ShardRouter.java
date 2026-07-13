package io.github.michalnowowiejski.distributedkv.sharding;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ShardRouter {

    private final Sharder sharder;
    private final int myIdx;
    private final Map<Integer, Shard> shardsByIdx;

    public ShardRouter(Sharder sharder, int myIdx, List<Shard> shards) {
        this.sharder = sharder;
        this.myIdx = myIdx;
        this.shardsByIdx = shards.stream().collect(Collectors.toMap(Shard::idx, shard -> shard));
    }

    public boolean isLocal(String key){
        return sharder.shardForKey(key) == myIdx;
    }

    public String ownerAddress(String key){
        int owner = sharder.shardForKey(key);
        Shard shard = shardsByIdx.get(owner);
        if (shard == null) {
            throw new IllegalArgumentException("No shard found for index: " + owner);
        }
        return shard.address();
    }
}
