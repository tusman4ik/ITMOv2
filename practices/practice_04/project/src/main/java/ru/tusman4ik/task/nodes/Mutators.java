package ru.tusman4ik.task.nodes;

import java.util.function.UnaryOperator;

/**
 * Каталог готовых мутаторов для {@link Nodes#mutated}.
 * Расширяется одной строкой.
 */
public final class Mutators {

    private Mutators() {}

    public static UnaryOperator<Boolean> flip() {
        return b -> !b;
    }

    public static UnaryOperator<Integer> shift(int delta) {
        return v -> v + delta;
    }

    public static UnaryOperator<Integer> negate() {
        return v -> -v;
    }
}
