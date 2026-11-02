package ru.tusman4ik.apicontracts.openapi;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Base(
        Class<?> base,
        String discriminatorProperty,
        List<Subtype> subtypes
) {
    public Base {
        Objects.requireNonNull(base, "Base must be not null");
        if (discriminatorProperty == null || discriminatorProperty.isBlank()) {
            throw new IllegalStateException("Blank discriminator property on " + base.getName());
        }
        Objects.requireNonNull(subtypes, "Subtypes must be not null on " + base.getName());
        if (subtypes.isEmpty()) {
            throw new IllegalStateException("Empty subtypes on " + base.getName());
        }
        Set<String> wireNames = new HashSet<>();
        for (Subtype subtype : subtypes) {
            if (!wireNames.add(subtype.wireName())) {
                throw new IllegalStateException(
                        "Duplicate subtype name '" + subtype.wireName() + "' on " + base.getName());
            }
        }
        subtypes = List.copyOf(subtypes);
    }
}
