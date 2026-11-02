package ru.tusman4ik.apicontracts.coderunner.submission;

import lombok.Builder;

import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.*;
import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.or;

/**
 * Per-field output caps for runner callbacks, in characters.
 *
 * <p>Bounds the size of debug/noise output a single submission can produce.
 * The number of cases is fixed by the request itself (task-service defines tests,
 * not the user), so per-field caps bound the total callback size too —
 * no separate body cap is needed.
 */
@Builder(toBuilder = true)
public record OutputLimitsDto(
        Integer maxStdoutChars,
        Integer maxStderrChars,
        Integer maxInputChars,
        Integer maxCheckerMsgChars,
        Integer maxCompilerLogChars
) {

    public static final OutputLimitsDto DEFAULT = OutputLimitsDto.builder()
            .maxStdoutChars(DEFAULT_MAX_STDOUT_CHARS)
            .maxStderrChars(DEFAULT_MAX_STDERR_CHARS)
            .maxInputChars(DEFAULT_MAX_INPUT_CHARS)
            .maxCheckerMsgChars(DEFAULT_MAX_CHECKER_MSG_CHARS)
            .maxCompilerLogChars(DEFAULT_MAX_COMPILER_LOG_CHARS)
            .build();

    public OutputLimitsDto {
        maxStdoutChars = or(maxStdoutChars, DEFAULT_MAX_STDOUT_CHARS);
        maxStderrChars = or(maxStderrChars, DEFAULT_MAX_STDERR_CHARS);
        maxInputChars = or(maxInputChars, DEFAULT_MAX_INPUT_CHARS);
        maxCheckerMsgChars = or(maxCheckerMsgChars, DEFAULT_MAX_CHECKER_MSG_CHARS);
        maxCompilerLogChars = or(maxCompilerLogChars, DEFAULT_MAX_COMPILER_LOG_CHARS);
    }
}
