package ru.tusman4ik.taskservice.task.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.tusman4ik.apicontracts.coderunner.callback.RunnerCallback;
import ru.tusman4ik.apicontracts.taskservice.api.RunnerCallbackApi;
import ru.tusman4ik.apicontracts.taskservice.check.CodeSubmissionFeedback;
import ru.tusman4ik.apicontracts.taskservice.check.SuccessCheck;
import ru.tusman4ik.taskservice.common.error.implementations.GeneratorNotCodeCapableException;
import ru.tusman4ik.taskservice.external.coderunner.ClientFeedbackSender;
import ru.tusman4ik.taskservice.external.coderunner.InMemoryVerdictStore;
import ru.tusman4ik.taskservice.task.generator.CodePendingChecker;
import ru.tusman4ik.taskservice.task.generator.ContextHolder;
import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.id.AttemptId;
import ru.tusman4ik.taskservice.task.wire.GeneratorRegistry;

@Slf4j
@RestController
public class RunnerCallbackController implements RunnerCallbackApi {

    private final GeneratorRegistry generatorRegistry;
    private final ContextHolder contextHolder;
    private final ClientFeedbackSender sender;
    private final InMemoryVerdictStore verdictStore;

    public RunnerCallbackController(GeneratorRegistry generatorRegistry,
                                    ContextHolder contextHolder,
                                    ClientFeedbackSender sender,
                                    InMemoryVerdictStore verdictStore) {
        this.generatorRegistry = generatorRegistry;
        this.contextHolder = contextHolder;
        this.sender = sender;
        this.verdictStore = verdictStore;
    }

    @Override
    public void onCallback(@Valid @RequestBody RunnerCallback callback) {
        log.info("onCallback received: seq={}, isLast={}, payloadType={}",
                callback.payload().seq(), callback.payload().isLast(), callback.payload().getClass().getSimpleName());

        AttemptId attempt = AttemptId.from(callback.attempt());
        PrototypeGenerator<?> generator = generatorRegistry.getGenerator(attempt.prototypeId());
        if (!(generator instanceof CodePendingChecker checker)) {
            throw new GeneratorNotCodeCapableException(attempt.prototypeId());
        }
        SuccessCheck verdict = contextHolder.warmed(
                attempt.instanceId(), () -> checker.checkCode(attempt, callback));
        log.info("VERDICT attempt={} scores={}/{} criteria={} rows={}",
                attempt, verdict.scores().earned(), verdict.scores().max(), verdict.criteria(),
                verdict.feedback() instanceof CodeSubmissionFeedback f ? f.rows().size() : 0);
        
        // Кэшируем только финальный или содержащий результаты тестов вердикт!
        if (callback.payload().isLast() || (verdict.feedback() instanceof CodeSubmissionFeedback f && !f.rows().isEmpty())) {
            verdictStore.put(attempt, verdict);
        }
        sender.send(attempt, verdict);
    }
}
