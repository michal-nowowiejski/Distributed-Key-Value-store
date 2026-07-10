package io.github.michalnowowiejski.distributedkv.storage;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
