package io.github.michalnowowiejski.distributedkv.sharding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ShardRouterTest {

    private static final List<Shard> SHARDS = List.of(
        new Shard("shard0", 0, "localhost:8080"),
        new Shard("shard1", 1, "localhost:8081"),
        new Shard("shard2", 2, "localhost:8082")
    );

    @Test
    void primaryAddressReturnsThePrimaryShardsAddress() {
        HashRing ring = new HashRing(SHARDS);
        ShardRouter router = new ShardRouter(ring, SHARDS.get(0), 1);

        for (int i = 0; i < 500; i++) {
            String key = "key" + i;
            Shard owner = ring.shardForKey(key);
            assertEquals(owner.address(), router.primaryAddress(key),
                "wrong primary address for " + key);
        }
    }

    @Test
    void isPrimaryIsTrueExactlyWhenKeyBelongsToMyShard() {
        HashRing ring = new HashRing(SHARDS);
        Shard self = SHARDS.get(1);
        ShardRouter router = new ShardRouter(ring, self, 1);

        for (int i = 0; i < 500; i++) {
            String key = "key" + i;
            boolean shouldBeLocal = ring.shardForKey(key).equals(self);
            assertEquals(shouldBeLocal, router.isPrimary(key), "isPrimary wrong for " + key);
        }
    }
    
}
