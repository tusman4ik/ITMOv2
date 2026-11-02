package ru.tusman4ik.task.context;

import ru.tusman4ik.task.nodes.Node;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

public final class Context {

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private final Random random;
    private final Map<Node<?>, Object> cache = new IdentityHashMap<>();

    public Context(long seed) {
        this.random = new Random(seed);
    }

    public static Context current() {
        Context ctx = CURRENT.get();
        if (ctx == null) {
            throw new IllegalStateException(
                    "No Context on this thread: node evaluated outside generate()");
        }
        return ctx;
    }

    public static Context replace(Context next) {
        Context prev = CURRENT.get();
        CURRENT.set(next);
        return prev;
    }

    public static void restore(Context prev) {
        CURRENT.set(prev);
    }

    @SuppressWarnings("unchecked")
    public <T> T cached(Node<T> node) {
        if (cache.containsKey(node)) {
            return (T) cache.get(node);
        }
        T value = node.get();
        cache.put(node, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T cached(Node<T> key, java.util.function.Supplier<T> compute) {
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

    public Random random() {
        return random;
    }
}
