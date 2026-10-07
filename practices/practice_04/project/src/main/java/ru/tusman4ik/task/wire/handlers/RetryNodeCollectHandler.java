package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.RetryExhaustedException;
import ru.tusman4ik.task.nodes.RetryableNodeException;
import ru.tusman4ik.task.nodes.annotations.RetryNodeDef;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.wire.Fields;
import ru.tusman4ik.task.wire.PrototypeHandler;

import java.lang.reflect.Field;

@Slf4j
public class RetryNodeCollectHandler implements PrototypeHandler {

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        Object generator = info.generator();
        int fields = 0;
        for (Field field : Fields.allOf(generator.getClass())) {
            RetryNodeDef def = field.getAnnotation(RetryNodeDef.class);
            if (def == null) {
                continue;
            }
            if (!Node.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("@RetryNodeDef on non-Node field " + field + " of " + id);
            }
            Node<?> raw = (Node<?>) Fields.read(generator, field);
            if (raw == null) {
                throw new IllegalStateException("@RetryNodeDef field not initialized " + field + " of " + id);
            }
            int n = def.maxAttempts();
            if (n <= 1) {
                throw new IllegalStateException(
                        "@RetryNodeDef maxAttempts must be > 1, got " + n + " on " + field + " of " + id);
            }
            String name = field.getName();
            log.trace("[{}] retry field enter: {} maxAttempts={}", id, name, n);
            Node<?> wrapped = () -> {
                RetryableNodeException last = null;
                for (int i = 0; i < n; i++) {
                    try {
                        return raw.get();
                    } catch (RetryableNodeException e) {
                        last = e;
                    }
                }
                throw new RetryExhaustedException(name, n, last);
            };
            Fields.write(generator, field, wrapped);
            fields++;
            log.debug("[{}] retry {} -> maxAttempts={}", id, name, n);
        }
        log.debug("[{}] retry-collect done: {} fields", id, fields);
    }
}
