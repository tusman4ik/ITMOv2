package ru.tusman4ik.taskservice.common.error.implementations;

import ru.tusman4ik.taskservice.common.error.DomainException;
import ru.tusman4ik.taskservice.common.error.TaskExceptionDef;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

public class InputSchemaMismatchException extends DomainException {

    public InputSchemaMismatchException(PrototypeId id, Class<?> expected, Class<?> actual) {
        super(TaskExceptionDef.INPUT_SCHEMA_MISMATCH, id,
                "expected input schema %s, got %s".formatted(expected.getName(), actual.getName()));
    }
}
