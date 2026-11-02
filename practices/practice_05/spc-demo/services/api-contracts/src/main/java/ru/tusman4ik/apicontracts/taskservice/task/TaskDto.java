package ru.tusman4ik.apicontracts.taskservice.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.tusman4ik.apicontracts.taskservice.form.InputForm;
import ru.tusman4ik.apicontracts.taskservice.id.InstanceIdDto;
import ru.tusman4ik.apicontracts.taskservice.task.content.Parameter;

import java.util.List;
import java.util.Objects;

@Schema(description = "Task instance")
public record TaskDto(
        @Schema(description = "Task identity") @NotNull @Valid InstanceIdDto instance,
        @Schema(description = "Task statement text") @NotNull @NotBlank String statement,
        @Schema(description = "Task parameters (structure depends on task type)") @NotNull List<@Valid Parameter> parameters,
        @Schema(description = "Suitable answer form (structure depends on task type)") @NotNull InputForm<?> inputForm
) {
    public TaskDto {
        Objects.requireNonNull(instance, "instance must be not null");
        Objects.requireNonNull(statement, "statement must be not null");
        if (statement.isBlank()) {
            throw new IllegalArgumentException("statement must be not blank");
        }
        Objects.requireNonNull(parameters, "parameters must be not null");
        parameters = List.copyOf(parameters);
        Objects.requireNonNull(inputForm, "inputForm must be not null");
    }
}