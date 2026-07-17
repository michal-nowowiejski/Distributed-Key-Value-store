package io.github.michalnowowiejski.distributedkv.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class Replicator {
    
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build();

    public void replicate(List<String> addresses, String key, byte[] value){
        CompletableFuture<?>[] futures = addresses.stream()
            .map(addr -> sendOne(addr, key, value))
            .toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(futures).join();
        } catch (CompletionException e) {
            throw new ReplicationException("replication failed", e.getCause());
        }
    }

    private CompletableFuture<Void> sendOne(String address, String key, byte[] value) {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://" + address + "/internal/replicate/" + key))
            .timeout(Duration.ofSeconds(2))
            .POST(HttpRequest.BodyPublishers.ofByteArray(value))
            .build();
        
        return http.sendAsync(req, HttpResponse.BodyHandlers.discarding())
            .thenAccept(res -> {
                if (res.statusCode() != 201)
                    throw new ReplicationException("replica " + address + " returned " + res.statusCode());
            });
    }
}
