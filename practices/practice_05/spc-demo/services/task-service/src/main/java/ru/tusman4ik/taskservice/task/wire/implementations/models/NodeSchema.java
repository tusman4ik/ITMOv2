package ru.tusman4ik.taskservice.task.wire.implementations.models;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@Builder
public record NodeSchema(Annotation annotation, String name, Field node) implements Comparable<NodeSchema> {

    @Override
    public int compareTo(@NonNull NodeSchema o) {
        return name.compareTo(o.name());
    }
}
