package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.task.context.Context;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.annotations.PickBoolNodeDef;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.wire.Fields;
import ru.tusman4ik.task.wire.PrototypeHandler;

import java.lang.reflect.Field;

@Slf4j
public class PickBoolNodeCollectHandler implements PrototypeHandler {

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        Object generator = info.generator();
        int fields = 0;
        for (Field field : Fields.allOf(generator.getClass())) {
            PickBoolNodeDef def = field.getAnnotation(PickBoolNodeDef.class);
            if (def == null) {
                continue;
            }
            if (!Node.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("@PickBoolNodeDef on non-Node field " + field + " of " + id);
            }
            if (Fields.read(generator, field) != null) {
                throw new IllegalStateException("@PickBoolNodeDef field must be null " + field + " of " + id);
            }
            double p = def.trueProbability();
            if (Double.isNaN(p) || p < 0.0 || p > 1.0) {
                throw new IllegalStateException(
                        "@PickBoolNodeDef trueProbability must be in [0,1], got " + p + " on " + field + " of " + id);
            }
            log.trace("[{}] pick-bool field enter: {} p={}", id, field.getName(), p);
            Node<Boolean> node = () -> Context.current().random().nextDouble() < p;
            Fields.write(generator, field, node);
            fields++;
            log.debug("[{}] pick-bool {} -> p={}", id, field.getName(), p);
        }
        log.debug("[{}] pick-bool-collect done: {} fields", id, fields);
    }
}
