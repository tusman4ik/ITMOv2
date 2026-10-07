package ru.tusman4ik.taskimpl.t1;

import ru.tusman4ik.task.checkers.Checkers;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.annotations.NodeDef;
import ru.tusman4ik.task.nodes.annotations.PickIntNodeDef;
import ru.tusman4ik.task.registry.annotations.Register;
import ru.tusman4ik.task.registry.enums.Exam;
import ru.tusman4ik.task.templates.Template;
import ru.tusman4ik.task.templates.annotations.TemplateDef;
import ru.tusman4ik.task.ui.forms.AnswerForms;

import static ru.tusman4ik.task.registry.enums.Subj.CS;

/**
 * Прототип 01-01: КОИ-8, вычёркивание из списка, −8 байт.
 * Слот: {@code @Register(EGE, CS, 1, 1)} → ресурсы {@code spc-res/01-01/}.
 * Варьируемый параметр — {@code datasetIdx} (0 — реки/Москва, 1 — озёра/Байкал).
 * Производные ноды читают один {@code spec}-нод (см. SKILL.md, запрет двойного RNG).
 */
@Register(
        exam = Exam.EGE,
        subject = CS,
        number = 1,
        prototype = 1
)
public final class Generator_1_1 extends Generator_1 {

    @PickIntNodeDef(min = 0, max = 1)
    private Node<Integer> datasetIdx;

    @NodeDef
    private Node<RiverTask> spec = () -> Generator_1.build(datasetIdx.get());

    @NodeDef
    private Node<String> statementText = () -> spec.get().statementText();

    @NodeDef
    private Node<String> answer = () -> spec.get().answer();

    @TemplateDef(0)
    private Template t0 = Template.of(
            AnswerForms.stringForm(),
            Checkers.trimmedString(() -> answer)
    );
}
