package ru.tusman4ik.taskservice.common.error.implementations;

import ru.tusman4ik.taskservice.common.error.DomainException;
import ru.tusman4ik.taskservice.common.error.TaskExceptionDef;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

public class UnknownPrototypeException extends DomainException {

    public UnknownPrototypeException(PrototypeId id) {
        super(TaskExceptionDef.PROTOTYPE_NOT_FOUND, id, "unknown prototype " + id);
    }
}
