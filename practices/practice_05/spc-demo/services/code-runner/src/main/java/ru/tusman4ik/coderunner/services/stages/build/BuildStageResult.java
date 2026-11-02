package ru.tusman4ik.coderunner.services.stages.build;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;

@Builder
public record BuildStageResult(
        Status status,
        String compilerLog
) {

    public static BuildStageResult ok() {
        return BuildStageResult.builder().status(Status.OK).build();
    }

    public static BuildStageResult error(String compileLog) {
        return BuildStageResult.builder()
                .status(Status.CE)
                .compilerLog(compileLog)
                .build();
    }
}
