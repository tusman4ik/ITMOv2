package ru.tusman4ik.coderunner.models.domain.results;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;

@Builder(toBuilder = true)
public record SandboxInvocationResult(
        Status status,

        Double cpuTimeSec,
        Double realTimeSec,
        Integer memoryKb,

        Integer exitCode,
        String stderr
) {
}
