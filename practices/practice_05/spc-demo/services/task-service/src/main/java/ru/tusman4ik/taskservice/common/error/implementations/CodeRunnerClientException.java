package ru.tusman4ik.taskservice.common.error.implementations;

import ru.tusman4ik.taskservice.common.error.DomainException;
import ru.tusman4ik.taskservice.common.error.TaskExceptionDef;

public class CodeRunnerClientException extends DomainException {

    public CodeRunnerClientException(String message) {
        super(TaskExceptionDef.CODE_RUNNER_UNAVAILABLE, null, message);
    }

    public CodeRunnerClientException(String message, Throwable cause) {
        super(TaskExceptionDef.CODE_RUNNER_UNAVAILABLE, null, message);
        initCause(cause);
    }
}
