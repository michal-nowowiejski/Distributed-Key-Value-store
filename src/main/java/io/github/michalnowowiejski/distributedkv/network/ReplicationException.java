package io.github.michalnowowiejski.distributedkv.network;

public class ReplicationException extends RuntimeException {
    public ReplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReplicationException(String message) {
        super(message);
    }
}
