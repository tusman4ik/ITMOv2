package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.Objects;

@Schema(description = "Failed check")
public record FailedCheck(
        @Schema(description = "Failure details") Map<String, Object> error
) implements CheckResult {
    public FailedCheck {
        Objects.requireNonNull(error, "error must be not null");
    }
}