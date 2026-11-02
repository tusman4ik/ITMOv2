package ru.tusman4ik.apicontracts.taskservice.task.content.attachments;

public record Meta(
        MimeType type,
        long approximateSizeBytes,
        String name
) {
}
