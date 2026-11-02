package ru.tusman4ik.apicontracts.taskservice.task.content.attachments;

import ru.tusman4ik.apicontracts.taskservice.id.InstanceIdDto;

public record AttachmentToken(
        InstanceIdDto id,
        String nodeName
) {
}
