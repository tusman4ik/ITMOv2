package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.Nodes;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.wire.Fields;
import ru.tusman4ik.task.wire.PrototypeHandler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class MemoizeHandler implements PrototypeHandler {

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        Object generator = info.generator();
        Map<String, Field> fields = new HashMap<>();
        for (Field field : Fields.allOf(generator.getClass())) {
            fields.putIfAbsent(field.getName(), field);
        }
        int done = 0, skipped = 0;
        for (Map.Entry<String, Node<?>> entry : info.nodes().entrySet()) {
            Node<?> node = entry.getValue();
            if (node instanceof Nodes.Memoized<?>) {
                skipped++;
                log.trace("[{}] memoize {}: already memoized, skipped", id, entry.getKey());
                continue;
            }
            Node<?> memoized = Nodes.memoized(node);
            Field field = fields.get(entry.getKey());
            if (field == null) {
                throw new IllegalStateException("no field for node " + entry.getKey() + " of " + id);
            }
            Fields.write(generator, field, memoized);
            entry.setValue(memoized);
            done++;
            log.trace("[{}] memoize {}: wrapped", id, entry.getKey());
        }
        log.debug("[{}] memoized {} nodes ({} already)", id, done, skipped);
    }
}
