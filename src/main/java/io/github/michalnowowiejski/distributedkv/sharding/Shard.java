package io.github.michalnowowiejski.distributedkv.sharding;

public record Shard(String name, int idx, String address) {

    public int port(){
        return Integer.parseInt(address.substring(address.lastIndexOf(':') + 1));
    }   
}