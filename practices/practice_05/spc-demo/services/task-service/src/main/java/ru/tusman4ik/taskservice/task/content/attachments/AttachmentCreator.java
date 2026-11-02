package ru.tusman4ik.taskservice.task.content.attachments;

import ru.tusman4ik.apicontracts.taskservice.task.content.attachments.Meta;

public interface AttachmentCreator {

    Meta getMeta();

    AttachmentFile getFile();
}
