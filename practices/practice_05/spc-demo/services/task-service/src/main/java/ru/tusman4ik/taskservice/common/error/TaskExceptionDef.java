package ru.tusman4ik.taskservice.common.error;

import org.springframework.http.HttpStatus;

public enum TaskExceptionDef implements ExceptionDef {

    PROTOTYPE_NOT_FOUND("Prototype not found", HttpStatus.NOT_FOUND, Severity.BUSINESS, false),
    GENERATOR_NOT_CODE_CAPABLE("Generator not code capable", HttpStatus.UNPROCESSABLE_ENTITY, Severity.BUSINESS, false),
    CLIENT_CALLBACK_FAILED("Client callback failed", HttpStatus.BAD_GATEWAY, Severity.INFRASTRUCTURE, true),
    INPUT_SCHEMA_MISMATCH("Input schema mismatch", HttpStatus.BAD_REQUEST, Severity.BUSINESS, false),
    CONTEXT_NOT_BOUND("Generation context not bound", HttpStatus.INTERNAL_SERVER_ERROR, Severity.UNEXPECTED, false),
    NODE_WARMING_FAILED("Task parameter generation failed", HttpStatus.INTERNAL_SERVER_ERROR, Severity.UNEXPECTED, false),
    CODE_RUNNER_UNAVAILABLE("Code-runner unavailable", HttpStatus.BAD_GATEWAY, Severity.INFRASTRUCTURE, true);

    private final String title;
    private final HttpStatus httpStatus;
    private final Severity severity;
    private final boolean retryable;

    TaskExceptionDef(String title, HttpStatus httpStatus, Severity severity, boolean retryable) {
        this.title = title;
        this.httpStatus = httpStatus;
        this.severity = severity;
        this.retryable = retryable;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String prefix() {
        return "TASK";
    }

    @Override
    public String reason() {
        return name();
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public Severity severity() {
        return severity;
    }

    @Override
    public boolean retryable() {
        return retryable;
    }
}
