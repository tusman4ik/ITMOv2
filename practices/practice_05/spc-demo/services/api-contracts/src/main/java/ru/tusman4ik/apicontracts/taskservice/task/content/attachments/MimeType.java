package ru.tusman4ik.apicontracts.taskservice.task.content.attachments;

import lombok.Getter;

@Getter
public enum MimeType {
    TEXT("text/plain"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation");

    private final String value;

    MimeType(String value) {this.value = value;}

}
