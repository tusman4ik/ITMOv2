package ru.tusman4ik.apicontracts.taskservice.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

@Schema(description = "Generated or recreated tasks")
public record TasksResponseDto(
        @Schema(description = "Task instances") @NotNull List<@Valid TaskDto> tasks
) {
    public TasksResponseDto {
        Objects.requireNonNull(tasks, "tasks must be not null");
        tasks = List.copyOf(tasks);
    }
}
