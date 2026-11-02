package ru.tusman4ik.apicontracts.taskservice.schema;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Text input schema")
public record TextInputSchema(
        String text
) implements InputSchema {
}
