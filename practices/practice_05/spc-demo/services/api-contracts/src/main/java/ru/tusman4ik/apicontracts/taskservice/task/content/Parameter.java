package ru.tusman4ik.apicontracts.taskservice.task.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Attachment.class, name = "attachment")
})
@Schema(description = "Polymorphic task parameter")
public sealed interface Parameter permits Attachment {
}
