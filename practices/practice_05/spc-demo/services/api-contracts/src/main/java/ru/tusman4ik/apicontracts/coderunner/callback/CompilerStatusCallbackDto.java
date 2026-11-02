package ru.tusman4ik.apicontracts.coderunner.callback;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;

@Builder
public record CompilerStatusCallbackDto(
        int seq,
        boolean isLast,

        Status status,
        String compilerLog,
        boolean truncated
)
        implements SubmissionCallbackDto {
    public static final String DISC = "compile_status";
}
