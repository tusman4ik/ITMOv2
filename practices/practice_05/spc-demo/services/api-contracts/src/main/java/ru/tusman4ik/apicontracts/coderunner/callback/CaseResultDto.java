package ru.tusman4ik.apicontracts.coderunner.callback;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;

@Builder
public record CaseResultDto (
        String caseId,
        String groupId,

        Status status,

        Double cpuTimeSec,
        Double realTimeSec,
        Integer memoryKb,

        String checkerMsg,

        String stdout,
        String stderr,
        String input,
        Integer exitCode,

        boolean truncated
){
}
