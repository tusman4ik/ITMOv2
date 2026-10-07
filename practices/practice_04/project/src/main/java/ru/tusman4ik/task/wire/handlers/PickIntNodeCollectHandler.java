package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.task.context.Context;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.annotations.PickIntNodeDef;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.wire.Fields;
import ru.tusman4ik.task.wire.PrototypeHandler;

import java.lang.reflect.Field;

@Slf4j
public class PickIntNodeCollectHandler implements PrototypeHandler {

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        Object generator = info.generator();
        int fields = 0;
        for (Field field : Fields.allOf(generator.getClass())) {
            PickIntNodeDef def = field.getAnnotation(PickIntNodeDef.class);
            if (def == null) {
                continue;
            }
            if (!Node.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("@PickIntNodeDef on non-Node field " + field + " of " + id);
            }
            if (Fields.read(generator, field) != null) {
                throw new IllegalStateException("@PickIntNodeDef field must be null " + field + " of " + id);
            }
            int min = def.min();
            int max = def.max();
            if (min >= max) {
                throw new IllegalStateException(
                        "@PickIntNodeDef requires min < max, got min=" + min + " max=" + max
                                + " on " + field + " of " + id);
            }
            log.trace("[{}] pick-int field enter: {} min={} max={}", id, field.getName(), min, max);
            Node<Integer> node = () -> Context.current().random().nextInt(min, max);
            Fields.write(generator, field, node);
            fields++;
            log.debug("[{}] pick-int {} -> [{}, {})", id, field.getName(), min, max);
        }
        log.debug("[{}] pick-int-collect done: {} fields", id, fields);
    }
}
