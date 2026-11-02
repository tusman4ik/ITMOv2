package ru.tusman4ik.taskservice.task.generator;

import ru.tusman4ik.taskservice.task.node.Node;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public record FrozenContext(Map<Node<?>, Object> snapshot) {

    public FrozenContext {
        snapshot = Collections.unmodifiableMap(snapshot);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOpt(Node<T> node) {
        if (!snapshot.containsKey(node)) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) snapshot.get(node));
    }

    public <T> T get(Node<T> node) {
        return getOpt(node).orElseThrow();
    }

    public boolean contains(Node<?> node) {
        return snapshot.containsKey(node);
    }

    public int size() {
        return snapshot.size();
    }
}
