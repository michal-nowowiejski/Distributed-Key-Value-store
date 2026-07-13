package io.github.michalnowowiejski.distributedkv.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.michalnowowiejski.distributedkv.sharding.Shard;

class ConfigLoaderTest {
    
    @Test 
    void loadsShardsFromValidYamlWithSingleShard(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("sharding.yaml");
        Files.writeString(file, """
            shards:
            - name: shard0
              idx: 0
              address: localhost:8080
            """);
        
        Config config = ConfigLoader.load(file.toString());
        List<Shard> shards = config.shards();
        assertEquals(1, shards.size());
        assertEquals(new Shard("shard0", 0, "localhost:8080"), shards.get(0));
    }

    @Test
    void loadsShardsFromValidYamlWithMultipleShards(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("sharding.yaml");
        Files.writeString(file, """
            shards:
            - name: shard0
              idx: 0
              address: localhost:8080
            - name: shard1
              idx: 1
              address: localhost:8081
            """);
        
        Config config = ConfigLoader.load(file.toString());
        List<Shard> shards = config.shards();
        assertEquals(2, shards.size());
        assertEquals(new Shard("shard0", 0, "localhost:8080"), shards.get(0));
        assertEquals(new Shard("shard1", 1, "localhost:8081"), shards.get(1));
    }

    @Test
    void loadsShardsFromValidYamlWithNoShards(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("sharding.yaml");
        Files.writeString(file, """
            shards: []
            """);
        
        Config config = ConfigLoader.load(file.toString());
        List<Shard> shards = config.shards();
        assertEquals(0, shards.size());
    }

    @Test
    void throwsWhenFileMissing(){
        var ex = assertThrows(RuntimeException.class, 
            () -> ConfigLoader.load("nonexistent.yaml"));
        assertTrue(ex.getMessage().contains("nonexistent.yaml"));
    }

    @Test
    void throwsWhenIdxNotANumber(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("bad.yaml");
                Files.writeString(file, """
            shards:
            - name: shard0
              idx: notanumber
              address: localhost:8080
            """);

        var ex = assertThrows(RuntimeException.class, 
            () -> ConfigLoader.load(file.toString()));
        assertTrue(ex.getMessage().contains("bad.yaml"));
    }
}
