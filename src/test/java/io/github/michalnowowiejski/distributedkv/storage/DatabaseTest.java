package io.github.michalnowowiejski.distributedkv.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseTest {

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void storesAndReturnsValue(@TempDir Path tempDir) {
        try (Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            db.setKey("key", bytes("value"));
            assertArrayEquals(bytes("value"), db.getKey("key"));
        }
    }

    @Test
    void returnsNullForMissingKey(@TempDir Path tempDir) {
        try (Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            assertNull(db.getKey("missing"));
        }
    }

    @Test
    void overwritesExistingValue(@TempDir Path tempDir) {
        try (Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            db.setKey("key", bytes("v1"));
            db.setKey("key", bytes("v2"));
            assertArrayEquals(bytes("v2"), db.getKey("key"));
        }
    }

    @Test
    void storesEmptyValueDistinctFromMissing(@TempDir Path tempDir) {
        try (Database db = Database.newDatabase(tempDir.resolve("db").toString())) {
            db.setKey("key", new byte[0]);
            byte[] value = db.getKey("key");
            assertNotNull(value);
            assertEquals(0, value.length);
        }
    }

    @Test
    void persistsAcrossReopen(@TempDir Path tempDir) {
        String path = tempDir.resolve("db").toString();
        try (Database db = Database.newDatabase(path)) {
            db.setKey("key", bytes("value"));
        }
        try (Database db = Database.newDatabase(path)) {
            assertArrayEquals(bytes("value"), db.getKey("key"));
        }
    }

    @Test
    void throwsWhenPathAlreadyOpen(@TempDir Path tempDir) {
        String path = tempDir.resolve("db").toString();
        try (Database first = Database.newDatabase(path)) {
            assertThrows(DatabaseException.class,
                () -> Database.newDatabase(path));
        }
    }
}
