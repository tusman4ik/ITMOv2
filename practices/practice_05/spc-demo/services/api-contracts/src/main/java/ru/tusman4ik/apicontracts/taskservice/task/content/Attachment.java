package ru.tusman4ik.apicontracts.taskservice.task.content;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.tusman4ik.apicontracts.taskservice.task.content.attachments.AttachmentToken;
import ru.tusman4ik.apicontracts.taskservice.task.content.attachments.Meta;

@Schema(description = "Attachment (meta + token)")
public record Attachment(
        Meta meta,
        AttachmentToken token
) implements Parameter {
}
