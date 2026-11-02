package ru.tusman4ik.apicontracts.taskservice.schema;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextInputSchema.class, name = "text-schema"),
        @JsonSubTypes.Type(value = IntegerSchema.class, name = "integer-schema")
})
@Schema(description = "Polymorphic input schema")
public sealed interface InputSchema permits IntegerSchema, TextInputSchema {
}
