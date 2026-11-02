package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.tusman4ik.apicontracts.taskservice.id.AttemptIdDto;

import java.util.Objects;

@Schema(description = "Check result with attempt identity")
public record CheckedItemDto(
        @Schema(description = "Attempt identity") @NotNull @Valid AttemptIdDto attempt,
        @Schema(description = "Check outcome") @NotNull @Valid CheckResult result
) {
    public CheckedItemDto {
        Objects.requireNonNull(attempt, "attempt must be not null");
        Objects.requireNonNull(result, "result must be not null");
    }
}
