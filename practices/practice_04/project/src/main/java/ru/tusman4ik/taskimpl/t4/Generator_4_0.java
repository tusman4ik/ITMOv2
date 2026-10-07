package ru.tusman4ik.taskimpl.t4;

import ru.tusman4ik.task.checkers.Checkers;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.annotations.NodeDef;
import ru.tusman4ik.task.registry.annotations.Register;
import ru.tusman4ik.task.registry.enums.Exam;
import ru.tusman4ik.task.templates.Template;
import ru.tusman4ik.task.templates.annotations.TemplateDef;
import ru.tusman4ik.task.ui.forms.AnswerForms;

import static ru.tusman4ik.task.registry.enums.Subj.CS;

@Register(
        exam = Exam.OGE,
        subject = CS,
        number = 4,
        prototype = 0
)
public final class Generator_4_0 extends Generator_4 {

    private static final GraphConfig CFG =
            new GraphConfig(
                    5,
                    2,
                    3,
                    1,
                    5,
                    2,
                    5);

    @NodeDef
    private Node<Integer> n = CFG::n;

    @NodeDef
    private Node<GraphSpec> spec = () -> build(ctx().random(), CFG);

    @NodeDef
    private Node<Integer> answer = () -> spec.get().answer();

    @NodeDef
    private Node<Matrix> matrix = () -> null;

    @NodeDef
    private Node<String> statementText = () -> {
        GraphSpec s = spec.get();
        return "Дан неориентированный взвешенный граф из " + s.n() + " вершин. "
                + "Таблица смежности (0 — ребра нет):\n" +
                "\nНайдите длину кратчайшего пути между вершинами "
                + (s.from() + 1) + " и " + (s.to() + 1) + ".";
    };

    @TemplateDef(99)
    private Template t99 = Template.of(
            AnswerForms.intForm(),
            Checkers.exactInt(() -> answer)
    ).raw(() -> matrix);

}
