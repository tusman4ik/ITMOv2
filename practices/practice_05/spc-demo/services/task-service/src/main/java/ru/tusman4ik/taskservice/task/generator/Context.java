package ru.tusman4ik.taskservice.task.generator;

import ru.tusman4ik.taskservice.task.id.InstanceId;
import ru.tusman4ik.taskservice.task.node.Node;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public record Context(
        Random random,
        InstanceId id,
        Map<Node<?>, Object> cache
) {
    public Context(InstanceId id) {
        this(new Random(id.seed()), id, new IdentityHashMap<>());
    }


    @SuppressWarnings("unchecked")
    public <T> T cached(Node<T> key, Supplier<T> compute) {
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        }
        T value = compute.get();
        cache.put(key, value);
        return value;
    }

    public FrozenContext freeze() {
        return new FrozenContext(new IdentityHashMap<>(cache));
    }
}
