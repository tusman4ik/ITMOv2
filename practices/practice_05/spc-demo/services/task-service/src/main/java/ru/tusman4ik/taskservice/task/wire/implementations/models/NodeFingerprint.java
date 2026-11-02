package ru.tusman4ik.taskservice.task.wire.implementations.models;

import org.jspecify.annotations.NonNull;
import ru.tusman4ik.taskservice.task.node.Node;

public record NodeFingerprint(
        NodeSchema shema,
        Node<?> node
) implements Comparable<NodeFingerprint> {
    @Override
    public int compareTo(@NonNull NodeFingerprint other) {
        return this.shema.compareTo(other.shema);
    }
}
