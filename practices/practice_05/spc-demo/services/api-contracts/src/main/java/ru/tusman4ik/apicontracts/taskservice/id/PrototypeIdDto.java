package ru.tusman4ik.apicontracts.taskservice.id;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;

@Schema(description = "Prototype identity")
public record PrototypeIdDto(
        @Schema(description = "Exam (e.g., OGE, EGE)") @NotNull Exam exam,
        @Schema(description = "Subject (e.g., MATH, INFORMATICS)") @NotNull Subject subject,
        @Schema(description = "Schoolbook number, 1..99") @NotNull @Min(1) @Max(99) Integer number,
        @Schema(description = "Prototype index, 0..999") @NotNull @Min(0) @Max(999) Integer prototypeIdx
) {
}