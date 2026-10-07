package ru.tusman4ik.taskimpl.t3;

import ru.tusman4ik.task.checkers.Checkers;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.annotations.NodeDef;
import ru.tusman4ik.task.nodes.annotations.PickBoolNodeDef;
import ru.tusman4ik.task.nodes.annotations.PickIntNodeDef;
import ru.tusman4ik.task.registry.annotations.Register;
import ru.tusman4ik.task.registry.enums.Exam;
import ru.tusman4ik.task.templates.Template;
import ru.tusman4ik.task.templates.annotations.TemplateDef;
import ru.tusman4ik.task.ui.forms.AnswerForms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.tusman4ik.task.registry.enums.Subj.CS;


@Register(
        exam = Exam.OGE,
        subject = CS,
        number = 3,
        prototype = 0
)
public class Generator_3_0 extends Generator_3 {

    @PickIntNodeDef(min = 40, max = 295)
    private Node<Integer> left;

    @PickIntNodeDef(min = 2, max = 6)
    private Node<Integer> length;

    @PickBoolNodeDef
    private Node<Boolean> statementWillBeTrue;

    @NodeDef
    private Node<RangePred> leftPred = () -> RangePred.greaterThan(left.get() - 1);

    @NodeDef
    private Node<RangePred> rightPred = () -> RangePred.lessThan(left.get() + length.get());

    record DivPredAndChosenNumber(int chosenNum, Expr divPred) {}

    record DivisorChoice(int d, boolean neg) {}

    @NodeDef
    private Node<Map<Integer, List<Integer>>> divisorsByNum = () -> {
        int l = left.get();
        int n = length.get();
        Map<Integer, List<Integer>> byNum = new HashMap<>();
        for (int x = l; x < l + n; x++) {
            byNum.put(x, divisors(x));
        }
        return byNum;
    };

    @NodeDef
    private Node<DivisorChoice> divisorChoice = () -> {
        int n = length.get();

        Map<Integer, Long> cnt = new HashMap<>();
        for (List<Integer> ds : divisorsByNum.get().values()) {
            for (int d : ds) {
                cnt.merge(d, 1L, Long::sum);
            }
        }

        List<Integer> positive = new ArrayList<>();
        List<Integer> inverted = new ArrayList<>();
        for (Map.Entry<Integer, Long> e : cnt.entrySet()) {
            if (e.getValue() == 1) {
                positive.add(e.getKey());
            }
            if (e.getValue() == n - 1) {
                inverted.add(e.getKey());
            }
        }
        if (positive.isEmpty() && inverted.isEmpty()) {
            throw new IllegalStateException("no candidates: " + divisorsByNum.get().keySet());
        }

        boolean neg;
        List<Integer> group;
        if (!positive.isEmpty() && !inverted.isEmpty()) {
            neg = bool();
            group = neg ? inverted : positive;
        } else if (!positive.isEmpty()) {
            neg = false;
            group = positive;
        } else {
            neg = true;
            group = inverted;
        }
        return new DivisorChoice(group.get(ctx().random().nextInt(group.size())), neg);
    };

    @NodeDef
    private Node<DivPredAndChosenNumber> divPred = () -> {
        DivisorChoice choice = divisorChoice.get();
        for (Map.Entry<Integer, List<Integer>> e : divisorsByNum.get().entrySet()) {
            if (e.getValue().contains(choice.d()) != choice.neg()) {
                Expr pred = choice.neg()
                        ? new DivPred(choice.d()).complement()
                        : new DivPred(choice.d());
                return new DivPredAndChosenNumber(e.getKey(), pred);
            }
        }
        throw new IllegalStateException("divisor disappeared: " + choice.d());
    };

    @NodeDef
    private Node<String> statement = () -> {
        List<Expr> parts = new ArrayList<>();
        parts.add(leftPred.get());
        parts.add(rightPred.get());
        parts.add(divPred.get().divPred());
        Expr conj = new AllOf(parts);
        Expr shown = statementWillBeTrue.get() ? conj : new NotPred(conj);
        return shown.restyle(ctx().random());
    };

    @NodeDef
    private Node<String> polarity = () -> statementWillBeTrue.get() ? "истинно" : "ложно";

    @NodeDef
    private Node<Integer> chosenNumber = () -> divPred.get().chosenNum();

    @TemplateDef(0)
    private Template t1 = Template.of(
            AnswerForms.intForm(),
            Checkers.exactInt(() -> chosenNumber)
    );

    private List<Integer> divisors(Integer x) {
        List<Integer> res = new ArrayList<>();
        for (int i = 2; i * i <= x; i++) {
            if (x % i == 0) {
                res.add(i);
                if (i * i != x) res.add(x / i);
            }
        }
        return res;
    }
}
