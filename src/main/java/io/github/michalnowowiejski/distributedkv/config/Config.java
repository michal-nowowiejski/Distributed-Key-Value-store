package io.github.michalnowowiejski.distributedkv.config;

import java.util.List;

import io.github.michalnowowiejski.distributedkv.sharding.Shard;

public record Config(List<Shard> shards) {}


