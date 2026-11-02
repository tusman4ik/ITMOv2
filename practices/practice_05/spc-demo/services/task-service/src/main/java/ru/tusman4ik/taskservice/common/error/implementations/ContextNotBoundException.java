package ru.tusman4ik.taskservice.common.error.implementations;

import ru.tusman4ik.taskservice.common.error.DomainException;
import ru.tusman4ik.taskservice.common.error.TaskExceptionDef;

public class ContextNotBoundException extends DomainException {

    public ContextNotBoundException() {
        super(TaskExceptionDef.CONTEXT_NOT_BOUND, null, "generation context is not bound on this call");
    }
}
