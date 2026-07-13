package io.github.michalnowowiejski.distributedkv.sharding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SharderTest {

    @Test
    void alwaysReturnsIndexWithinRange() {
        Sharder sharder = new Sharder(3);
        for (int i = 0; i < 1000; i++) {
            int shard = sharder.shardForKey("key" + i);
            assertTrue(shard >= 0 && shard < 3, "shard out of range: " + shard);
        }
    }

    @Test
    void sameKeyAlwaysMapsToSameShard() {
        Sharder sharder = new Sharder(5);
        int first = sharder.shardForKey("user");
        for (int i = 0; i < 100; i++) {
            assertEquals(first, sharder.shardForKey("user"));
        }
    }

    @Test
    void differentInstancesAgreeForSameKey() {
        Sharder a = new Sharder(5);
        Sharder b = new Sharder(5);
        assertEquals(a.shardForKey("user"), b.shardForKey("user"));
    }

    @Test
    void spreadsKeysAcrossAllShards() {
        Sharder sharder = new Sharder(3);
        int[] counts = new int[3];
        for (int i = 0; i < 3000; i++) {
            counts[sharder.shardForKey("user:" + i)]++;
        }
        for (int i = 0; i < counts.length; i++) {
            assertTrue(counts[i] > 0, "shard " + i + " got no keys");
        }
    }

    @Test
    void rejectsInvalidShardCount() {
        assertThrows(IllegalArgumentException.class, () -> new Sharder(0));
        assertThrows(IllegalArgumentException.class, () -> new Sharder(-1));
    }
}