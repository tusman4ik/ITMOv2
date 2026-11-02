package ru.tusman4ik.taskservice.task.wire;

import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;
import ru.tusman4ik.taskservice.common.error.implementations.InputSchemaMismatchException;
import ru.tusman4ik.taskservice.common.error.implementations.UnknownPrototypeException;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

import java.util.Collections;
import java.util.Map;

public record SchemaRegistry(Map<PrototypeId, Class<? extends InputSchema>> schemas) {

    public SchemaRegistry {
        schemas = Map.copyOf(schemas);
    }

    @Override
    public Map<PrototypeId, Class<? extends InputSchema>> schemas() {
        return Collections.unmodifiableMap(schemas);
    }

    public void require(PrototypeId id, InputSchema schema) {
        Class<?> expected = schemas.get(id);
        if (expected == null) {
            throw new UnknownPrototypeException(id);
        }
        if (!expected.isInstance(schema)) {
            throw new InputSchemaMismatchException(id, expected, schema.getClass());
        }
    }
}
