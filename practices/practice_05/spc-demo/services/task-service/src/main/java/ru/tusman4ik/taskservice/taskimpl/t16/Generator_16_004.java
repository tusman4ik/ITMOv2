package ru.tusman4ik.taskservice.taskimpl.t16;

import org.springframework.beans.factory.annotation.Value;
import ru.tusman4ik.apicontracts.coderunner.callback.RunnerCallback;
import ru.tusman4ik.apicontracts.taskservice.check.CheckResult;
import ru.tusman4ik.apicontracts.taskservice.check.SuccessCheck;
import ru.tusman4ik.apicontracts.taskservice.schema.TextInputSchema;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;
import ru.tusman4ik.taskservice.external.coderunner.CodeRunnerClient;
import ru.tusman4ik.taskservice.task.generator.Register;
import ru.tusman4ik.taskservice.task.id.AttemptId;
import ru.tusman4ik.taskservice.task.node.Node;
import ru.tusman4ik.taskservice.task.node.implementations.NodeDef;
import ru.tusman4ik.taskservice.taskimpl.t16.common.BaseCodePendingGenerator;
import ru.tusman4ik.taskservice.taskimpl.t16.common.NumberSequenceUtils;
import ru.tusman4ik.taskservice.taskimpl.t16.common.TestCase;

import java.util.List;

@Register(
        prototypeIdx = 4,
        exam = Exam.OGE,
        subject = Subject.CS,
        number = 16
)
public class Generator_16_004 extends BaseCodePendingGenerator {

    public Generator_16_004(CodeRunnerClient codeRunnerClient,
                            @Value("${task-service.public-base-url:http://localhost:8080}") String callbacksBaseUrl) {
        super(codeRunnerClient, callbacksBaseUrl);
    }

    @Override
    public CheckResult check(AttemptId id, TextInputSchema schema) {
        return super.check(id, schema, this.tests.get());
    }

    @Override
    public SuccessCheck checkCode(AttemptId id, RunnerCallback callback) {
        return super.checkCode(id, callback, this.tests.get());
    }

    @NodeDef
    private Node<Integer> k = () -> 2 + ctx().random().nextInt(7);

    @NodeDef
    private Node<List<TestCase>> tests = () ->
            NumberSequenceUtils.generateSuite16_004(k.get(), ctx().random());

    @NodeDef
    private Node<String>
            firstExampleIn = () -> tests.get().getFirst().in(),
            firstExampleExp = () -> tests.get().getFirst().expected(),
            secondExampleIn = () -> tests.get().get(1).in(),
            secondExampleExp = () -> tests.get().get(1).expected();
}
