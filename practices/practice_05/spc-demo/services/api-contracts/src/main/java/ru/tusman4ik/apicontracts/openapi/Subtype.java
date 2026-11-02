package ru.tusman4ik.apicontracts.openapi;

import java.util.Objects;

public record Subtype(String wireName, Class<?> type) {
    public Subtype {
        Objects.requireNonNull(type, "Subtype type must be not null");
        if (wireName == null || wireName.isBlank()) {
            throw new IllegalStateException("Blank subtype wire name for " + type.getName());
        }
    }
}
