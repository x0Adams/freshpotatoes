package hu.pogany.freshPotato.entity;

import java.util.UUID;

public interface UuidPrimaryKey {
    void setUuid(String uuid);
    default void generateUuid() {
        setUuid(UUID.randomUUID().toString());
    }
}
