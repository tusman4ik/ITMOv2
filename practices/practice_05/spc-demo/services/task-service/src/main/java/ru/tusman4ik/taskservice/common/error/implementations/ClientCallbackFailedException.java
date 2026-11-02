package ru.tusman4ik.taskservice.common.error.implementations;

import ru.tusman4ik.taskservice.common.error.DomainException;
import ru.tusman4ik.taskservice.common.error.TaskExceptionDef;

public class ClientCallbackFailedException extends DomainException {

    public ClientCallbackFailedException(String detail) {
        super(TaskExceptionDef.CLIENT_CALLBACK_FAILED, detail, "client callback delivery failed: " + detail);
    }
}
