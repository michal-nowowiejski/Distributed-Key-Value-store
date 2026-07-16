package io.github.michalnowowiejski.distributedkv.config;

import java.util.List;

import io.github.michalnowowiejski.distributedkv.sharding.Shard;

public record Config(List<Shard> shards) {

    public Shard shardByName(String name){
        return shards.stream()
            .filter(s -> s.name().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Shard not found: " + name)); 
        }
}