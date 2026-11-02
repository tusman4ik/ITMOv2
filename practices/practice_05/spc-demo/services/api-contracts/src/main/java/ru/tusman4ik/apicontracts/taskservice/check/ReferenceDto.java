package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.tusman4ik.apicontracts.taskservice.check.enums.ReferenceKind;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;

import java.util.Objects;

@Schema(description = "Reference answer")
public record ReferenceDto(
        @Schema(description = "Reference answer, null when kind is NONE") InputSchema answer,
        @Schema(description = "Reference kind") ReferenceKind kind
) {
    public ReferenceDto {
        Objects.requireNonNull(kind, "kind must be not null");
        if ((kind == ReferenceKind.NONE) == (answer != null)) {
            throw new IllegalArgumentException(
                    "kind=NONE requires null answer, other kinds require non-null answer, got: " + kind);
        }
    }
}