package ru.tusman4ik.taskimpl.t3;

import ru.tusman4ik.task.generators.PrototypeGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class Generator_3 extends PrototypeGenerator {

    private static final double NOT_PROB = 0.2;

    public sealed interface Expr permits DivPred, NotPred, RangePred, AllOf, AnyOf {
        String restyle(Random rng);

        Expr complement();
    }

    public record NotPred(Expr child) implements Expr {
        @Override
        public String restyle(Random rng) {
            if (child instanceof AllOf || child instanceof AnyOf) {
                return demorgan(child, rng);
            }
            return "НЕ " + child.restyle(rng);
        }

        @Override
        public Expr complement() {
            return child;
        }
    }

    public record RangePred(int bound, boolean isLess) implements Expr {

        public static RangePred lessThan(int num) {
            return new RangePred(num, true);
        }

        public static RangePred greaterThan(int num) {
            return new RangePred(num, false);
        }

        @Override
        public Expr complement() {
            // Строгое каноническое представление: ¬(X<B) ⟺ (X>B-1), ¬(X>B) ⟺ (X<B+1).
            // Без сдвига границы — off-by-one на краю отрезка.
            return isLess ? new RangePred(bound - 1, false) : new RangePred(bound + 1, true);
        }

        @Override
        public String restyle(Random rng) {
            return switch (rng.nextInt(4)) {
                case 0 -> isLess ? "(X < " + bound + ")" : "(X > " + bound + ")";
                case 1 -> isLess ? "(X <= " + (bound - 1) + ")" : "(X >= " + (bound + 1) + ")";
                case 2 -> isLess ? "(" + bound + " > X)" : "(" + bound + " < X)";
                default -> isLess ? "(" + (bound - 1) + " >= X)" : "(" + (bound + 1) + " <= X)";
            };
        }
    }

    public record DivPred(int d, boolean neg) implements Expr {

        public DivPred(int d) {
            this(d, false);
        }

        @Override
        public Expr complement() {
            return new DivPred(d, !neg);
        }

        @Override
        public String restyle(Random rng) {
            if (!neg) {
                return switch (rng.nextInt(3)) {
                    case 0 -> "(X кратно " + d + ")";
                    case 1 -> "(X делится на " + d + ")";
                    default -> "(" + d + " делит X)";
                };
            }
            return switch (rng.nextInt(3)) {
                case 0 -> "(X не кратно " + d + ")";
                case 1 -> "(X не делится на " + d + ")";
                default -> "(" + d + " не делит X)";
            };
        }
    }

    public record AllOf(List<Expr> parts) implements Expr {

        @Override
        public Expr complement() {
            return new NotPred(this);
        }

        @Override
        public String restyle(Random rng) {
            List<Expr> shuffled = new ArrayList<>(parts);
            Collections.shuffle(shuffled, rng);
            List<String> texts = new ArrayList<>();
            for (Expr p : shuffled) {
                texts.add(maybeNot(p, rng).restyle(rng));
            }
            return String.join(" И ", texts);
        }
    }

    public record AnyOf(List<Expr> parts) implements Expr {

        @Override
        public Expr complement() {
            return new NotPred(this);
        }

        @Override
        public String restyle(Random rng) {
            List<Expr> shuffled = new ArrayList<>(parts);
            Collections.shuffle(shuffled, rng);
            List<String> texts = new ArrayList<>();
            for (Expr p : shuffled) {
                texts.add(maybeNot(p, rng).restyle(rng));
            }
            return String.join(" ИЛИ ", texts);
        }
    }

    private static Expr maybeNot(Expr p, Random rng) {
        if (rng.nextDouble() < NOT_PROB) {
            return new NotPred(p.complement());
        }
        return p;
    }

    private static String demorgan(Expr group, Random rng) {
        boolean isAnd = group instanceof AllOf;
        List<Expr> parts = isAnd ? ((AllOf) group).parts() : ((AnyOf) group).parts();
        List<String> texts = new ArrayList<>();
        for (Expr p : parts) {
            texts.add(p.complement().restyle(rng));
        }
        return String.join(isAnd ? " ИЛИ " : " И ", texts);
    }
}
