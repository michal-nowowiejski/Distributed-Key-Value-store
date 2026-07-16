package io.github.michalnowowiejski.distributedkv.sharding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class HashRingTest {

    private static final List<Shard> SHARDS = List.of(
        new Shard("shard0", 0, "localhost:8080"),
        new Shard("shard1", 1, "localhost:8081"),
        new Shard("shard2", 2, "localhost:8082"),
        new Shard("shard3", 3, "localhost:8083")
    );

    @Test
    void sameKeyAlwaysMapsToSameShard() {
        HashRing ring = new HashRing(SHARDS);
        Shard first = ring.shardForKey("user:42");
        for (int i = 0; i < 100; i++) {
            assertEquals(first, ring.shardForKey("user:42"));
        }
    }

    @Test
    void ringsWithSameShardsAgree() {
        HashRing a = new HashRing(SHARDS);
        HashRing b = new HashRing(SHARDS);
        for (int i = 0; i < 500; i++) {
            String key = "key" + i;
            assertEquals(a.shardForKey(key), b.shardForKey(key), "rings disagree on " + key);
        }
    }

    @Test
    void distributesKeysAcrossAllShards(){
        HashRing ring = new HashRing(SHARDS);
        Map<Shard, Integer> counts = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            counts.merge(ring.shardForKey("key" + i), 1, Integer::sum);
        }
        for (Shard shard : SHARDS) {
            assertTrue(counts.getOrDefault(shard, 0) > 0, shard.name() + " got no keys");
        }
    }

    @Test
    void addingNodeRemapsFewKeys() {
        int keyCount = 10000;

        HashRing before = new HashRing(SHARDS);
        Map<String, Shard> ownerBefore = new HashMap<>();
        for (int i = 0; i < keyCount; i++) {
            String key = "key" + i;
            ownerBefore.put(key, before.shardForKey(key));
        }

        List<Shard> withNewNode = new ArrayList<>(SHARDS);
        withNewNode.add(new Shard("shard4", 4, "localhost:8084"));
        HashRing after = new HashRing(withNewNode);

        int moved = 0;
        for (int i = 0; i < keyCount; i++) {
            String key = "key" + i;
            if (!after.shardForKey(key).equals(ownerBefore.get(key))) moved++;
        }

        double fraction = (double) moved / keyCount;
        // expected ~ 1/(N+1) = 1/5 = 0.2
        assertTrue(fraction < 0.3,
            "consistent hashing should remap ~1/(N+1), but moved " + fraction);
    }

    @Test
    void rejectsEmptyRing(){
        assertThrows(IllegalArgumentException.class, () -> new HashRing(new ArrayList<>()));
    }
}