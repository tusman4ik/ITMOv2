package ru.tusman4ik.taskservice.external.coderunner;

import org.springframework.stereotype.Component;
import ru.tusman4ik.apicontracts.taskservice.check.SuccessCheck;
import ru.tusman4ik.taskservice.task.id.AttemptId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryVerdictStore {

    private final Map<String, SuccessCheck> verdicts = new ConcurrentHashMap<>();

    public void put(AttemptId attemptId, SuccessCheck verdict) {
        verdicts.put(attemptId.attempt().toString(), verdict);
    }

    public SuccessCheck get(AttemptId attemptId) {
        return verdicts.get(attemptId.attempt().toString());
    }

    public SuccessCheck get(String attemptUuid) {
        return verdicts.get(attemptUuid);
    }
}
