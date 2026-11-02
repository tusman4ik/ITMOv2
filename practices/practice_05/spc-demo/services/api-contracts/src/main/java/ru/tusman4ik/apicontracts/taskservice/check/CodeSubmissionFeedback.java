package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;

import java.util.List;
import java.util.Objects;

@Builder(toBuilder = true)
@Schema(description = "Full code attempt table: every test rendered with input, output and reference answer visible")
public record CodeSubmissionFeedback(
        @Schema(description = "Per-test rows, in execution order") List<Row> rows,
        @Schema(description = "Passed tests") int passed,
        @Schema(description = "Total rendered tests") int total,
        @Schema(description = "Compiler log, null when compilation succeeded") String compilerLog,
        @Schema(description = "True if any field was truncated by output limits") boolean truncated,
        @Schema(description = "Infrastructure error details, null on normal runs") String error
) implements Feedback {

    @Builder
    @Schema(description = "One rendered test case")
    public record Row(
            @Schema(description = "Test case id") String caseId,
            @Schema(description = "Test group id") String groupId,
            @Schema(description = "Outcome") Status status,
            @Schema(description = "Program input") String input,
            @Schema(description = "Captured program output") String stdout,
            @Schema(description = "Reference answer") String expected,
            @Schema(description = "Checker message") String checkerMsg,
            @Schema(description = "CPU seconds") Double cpuTimeSec,
            @Schema(description = "Wall seconds") Double realTimeSec,
            @Schema(description = "Peak memory, Kb") Integer memoryKb,
            @Schema(description = "Process exit code") Integer exitCode
    ) {
    }

    public CodeSubmissionFeedback {
        Objects.requireNonNull(rows, "rows must be not null");
        rows = List.copyOf(rows);
    }
}
