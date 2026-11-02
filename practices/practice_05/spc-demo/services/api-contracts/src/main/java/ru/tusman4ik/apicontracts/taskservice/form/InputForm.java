package ru.tusman4ik.apicontracts.taskservice.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CodeInputForm.class, name = "code-form"),
})
@Schema(description = "Polymorphic answer form")
public sealed interface InputForm<I extends InputSchema> permits CodeInputForm {

    @JsonProperty("schema")
    default I getSchema() {return null;}


}
