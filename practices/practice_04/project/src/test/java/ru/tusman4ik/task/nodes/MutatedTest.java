package ru.tusman4ik.task.nodes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.tusman4ik.task.context.Context;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MutatedTest {

    @AfterEach
    void cleanup() {
        Context.restore(null);
    }

    private static void within(long seed, java.util.function.Consumer<Context> body) {
        Context prev = Context.replace(new Context(seed));
        try {
            body.accept(Context.current());
        } finally {
            Context.restore(prev);
        }
    }

    @Test
    void zeroProbabilityAlwaysPassesThrough() {
        within(1L, ctx -> {
            Node<Integer> base = () -> 42;
            Node<Integer> noisy = Nodes.mutated(base, 0.0, Mutators.shift(10));
            for (int i = 0; i < 100; i++) {
                assertEquals(42, noisy.get());
            }
        });
    }

    @Test
    void oneProbabilityAlwaysMutates() {
        within(1L, ctx -> {
            Node<Boolean> base = () -> true;
            Node<Boolean> noisy = Nodes.mutated(base, 1.0, Mutators.flip());
            for (int i = 0; i < 100; i++) {
                assertEquals(false, noisy.get());
            }
        });
    }

    @Test
    void statisticsRoughlyMatchProbability() {
        int total = 10_000;
        final int[] mutated = new int[1];
        within(42L, ctx -> {
            Node<Integer> noisy = Nodes.mutated(() -> 0, 0.3, v -> 1);
            for (int i = 0; i < total; i++) {
                mutated[0] += noisy.get();
            }
        });
        double rate = (double) mutated[0] / total;
        assertTrue(rate > 0.25 && rate < 0.35, "rate=" + rate);
    }

    @Test
    void deterministicPerSeed() {
        Node<Integer> noisy = Nodes.mutated(() -> 7, 0.5, Mutators.negate());
        int[] first = new int[1];
        within(123L, ctx -> first[0] = noisy.get());
        within(123L, ctx -> assertEquals(first[0], noisy.get()));
    }

    @Test
    void sourceEvaluatedOnce() {
        AtomicInteger calls = new AtomicInteger();
        Node<Integer> base = () -> {
            calls.incrementAndGet();
            return 5;
        };
        Node<Integer> noisy = Nodes.mutated(base, 0.5, Mutators.shift(1));
        within(7L, ctx -> noisy.get());
        assertEquals(1, calls.get());
    }

    @Test
    void validationFailsFast() {
        Node<Integer> base = () -> 1;
        assertThrows(IllegalArgumentException.class,
                () -> Nodes.mutated(base, Double.NaN, Mutators.shift(1)));
        assertThrows(IllegalArgumentException.class,
                () -> Nodes.mutated(base, -0.1, Mutators.shift(1)));
        assertThrows(IllegalArgumentException.class,
                () -> Nodes.mutated(base, 1.5, Mutators.shift(1)));
        assertThrows(IllegalArgumentException.class,
                () -> Nodes.mutated(null, 0.5, Mutators.shift(1)));
        assertThrows(IllegalArgumentException.class,
                () -> Nodes.mutated(base, 0.5, null));
    }
}
