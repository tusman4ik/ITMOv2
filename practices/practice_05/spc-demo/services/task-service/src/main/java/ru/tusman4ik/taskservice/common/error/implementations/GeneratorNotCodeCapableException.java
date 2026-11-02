package ru.tusman4ik.taskservice.common.error.implementations;

import ru.tusman4ik.taskservice.common.error.DomainException;
import ru.tusman4ik.taskservice.common.error.TaskExceptionDef;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

public class GeneratorNotCodeCapableException extends DomainException {

    public GeneratorNotCodeCapableException(PrototypeId id) {
        super(TaskExceptionDef.GENERATOR_NOT_CODE_CAPABLE, id,
                "generator " + id + " does not handle code callbacks");
    }
}
