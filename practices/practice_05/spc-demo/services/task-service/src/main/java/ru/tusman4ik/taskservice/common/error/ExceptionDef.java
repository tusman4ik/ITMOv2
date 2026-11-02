package ru.tusman4ik.taskservice.common.error;

import org.springframework.http.HttpStatus;

public interface ExceptionDef {
    default String code() {
        return prefix() + "." + reason();
    }

    String title();

    String prefix();

    String reason();

    HttpStatus httpStatus();

    Severity severity();

    boolean retryable();
}
