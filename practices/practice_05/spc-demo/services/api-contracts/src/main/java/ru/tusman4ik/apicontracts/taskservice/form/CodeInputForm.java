package ru.tusman4ik.apicontracts.taskservice.form;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.tusman4ik.apicontracts.taskservice.schema.TextInputSchema;

@Schema(description = "Input form for code")
public record CodeInputForm(
        Language language
) implements InputForm<TextInputSchema> {

    public enum Language {
        PYTHON
    }
}
