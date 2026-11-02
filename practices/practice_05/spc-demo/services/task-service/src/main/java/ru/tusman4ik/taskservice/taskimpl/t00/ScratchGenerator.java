package ru.tusman4ik.taskservice.taskimpl.t00;

import ru.tusman4ik.apicontracts.taskservice.check.CheckResult;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;
import ru.tusman4ik.taskservice.task.content.attachments.AttachmentCreator;
import ru.tusman4ik.taskservice.task.content.attachments.AttachmentFile;
import ru.tusman4ik.apicontracts.taskservice.task.content.attachments.Meta;
import ru.tusman4ik.apicontracts.taskservice.form.CodeInputForm;
import ru.tusman4ik.apicontracts.taskservice.form.InputForm;
import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.generator.Register;
import ru.tusman4ik.taskservice.task.id.AttemptId;
import ru.tusman4ik.taskservice.task.node.Node;
import ru.tusman4ik.taskservice.task.node.implementations.AttachmentNodeDef;
import ru.tusman4ik.taskservice.task.node.implementations.NodeDef;
import ru.tusman4ik.apicontracts.taskservice.schema.TextInputSchema;

@Register(
        number = 1,
        subject = Subject.CS,
        exam = Exam.EGE,
        prototypeIdx = 1
)
public class ScratchGenerator extends PrototypeGenerator<TextInputSchema> {

    @NodeDef
    private Node<Integer> nodeNanan = () -> 1;

    @AttachmentNodeDef
    private Node<AttachmentCreator> node1 = () -> new AttachmentCreator() {
        @Override
        public Meta getMeta() {
            return null;
        }

        @Override
        public AttachmentFile getFile() {
            return null;
        }
    };

    @Override
    public InputForm<? extends TextInputSchema> form() {
        return new CodeInputForm(CodeInputForm.Language.PYTHON);
    }

    @Override
    public CheckResult check(AttemptId id, TextInputSchema schema) {
        return null;
    }

}
