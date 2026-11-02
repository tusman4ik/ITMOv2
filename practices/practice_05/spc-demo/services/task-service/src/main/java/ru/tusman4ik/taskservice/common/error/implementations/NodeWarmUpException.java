package ru.tusman4ik.taskservice.common.error.implementations;

import ru.tusman4ik.taskservice.common.error.DomainException;
import ru.tusman4ik.taskservice.common.error.TaskExceptionDef;
import ru.tusman4ik.taskservice.task.id.InstanceId;

public class NodeWarmUpException extends DomainException {

    public NodeWarmUpException(InstanceId id, String detail) {
        super(TaskExceptionDef.NODE_WARMING_FAILED, id, detail);
    }

    public NodeWarmUpException(InstanceId id, String detail, Throwable cause) {
        super(TaskExceptionDef.NODE_WARMING_FAILED, id, detail);
        initCause(cause);
    }
}
