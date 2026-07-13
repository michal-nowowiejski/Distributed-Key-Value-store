package io.github.michalnowowiejski.distributedkv.sharding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ShardRouterTest {

    private static final List<Shard> SHARDS = List.of(
        new Shard("shard2", 2, "localhost:8082"),
        new Shard("shard0", 0, "localhost:8080"),
        new Shard("shard1", 1, "localhost:8081")
    );

    @Test
    void ownerAddressReturnsTheOwningShardsAddress() {
        Sharder sharder = new Sharder(3);
        ShardRouter router = new ShardRouter(sharder, 0, SHARDS);

        for (int i = 0; i < 500; i++) {
            String key = "key" + i;
            int owner = sharder.shardForKey(key);
            assertEquals(addressOfShard(owner), router.ownerAddress(key),
                "wrong owner address for " + key);
        }
    }

    @Test
    void isLocalIsTrueExactlyWhenKeyBelongsToMyShard() {
        Sharder sharder = new Sharder(3);
        int myIdx = 1;
        ShardRouter router = new ShardRouter(sharder, myIdx, SHARDS);

        for (int i = 0; i < 500; i++) {
            String key = "key" + i;
            boolean shouldBeLocal = sharder.shardForKey(key) == myIdx;
            assertEquals(shouldBeLocal, router.isLocal(key), "isLocal wrong for " + key);
        }
    }

    private static String addressOfShard(int idx) {
        return SHARDS.stream()
            .filter(s -> s.idx() == idx)
            .findFirst().orElseThrow()
            .address();
    }
}
