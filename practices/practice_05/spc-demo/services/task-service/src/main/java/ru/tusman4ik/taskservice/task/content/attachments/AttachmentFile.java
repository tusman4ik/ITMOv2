package ru.tusman4ik.taskservice.task.content.attachments;

public record AttachmentFile(
        byte[] bytes
) {

    public long length() {return bytes.length;}
}
