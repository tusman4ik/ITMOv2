package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.annotations.PickNodeDef;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.resources.Loader;
import ru.tusman4ik.task.resources.pick.PickLocation;
import ru.tusman4ik.task.resources.pick.PickNodeFactory;
import ru.tusman4ik.task.resources.pick.PickShape;
import ru.tusman4ik.task.wire.Fields;
import ru.tusman4ik.task.wire.PrototypeHandler;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
public class PickNodeCollectHandler implements PrototypeHandler {

    private final Loader loader;

    public PickNodeCollectHandler(Loader loader) {
        this.loader = loader;
    }

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        Object generator = info.generator();
        int fields = 0;
        for (Field field : Fields.allOf(generator.getClass())) {
            PickNodeDef def = field.getAnnotation(PickNodeDef.class);
            if (def == null) {
                continue;
            }
            if (!Node.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("@PickNodeDef on non-Node field " + field + " of " + id);
            }
            PickShape shape = PickShape.of(field, def);

            log.trace("[{}] pick field enter: {}", id, field.getName());
            log.trace("[{}] pick {} shape: row={} n={} distinct={}", id, field.getName(),
                    shape.row().getSimpleName(), shape.n(), shape.distinct());

            List<? extends Predicate<?>> predicates = PickNodeFactory.predicatesOf(Fields.read(generator, field));
            PickLocation.Resolved resolved = PickLocation.resolve(loader, id, shape.row(), def);

            log.trace("[{}] pick {} location: strategy={} location={}", id, field.getName(),
                    resolved.strategy(), resolved.location());

            List<?> data = getObjects(id, field, resolved, shape);

            log.trace("[{}] pick {} loaded: dataSize={} mode={}", id, field.getName(),
                    data.size(), PickNodeFactory.samplerMode(shape));

            Node<?> node = PickNodeFactory.build(data, shape, predicates);
            Fields.write(generator, field, node);
            fields++;
            log.debug("[{}] pick {} -> {} x{}{} from {}, predicates={}, dataSize={}",
                    id, field.getName(), shape.row().getSimpleName(), shape.n(),
                    shape.distinct() ? " distinct" : "", resolved.location(), predicates.size(), data.size());
        }
        log.debug("[{}] pick-collect done: {} fields", id, fields);
    }

    private static List<?> getObjects(PrototypeId id, Field field, PickLocation.Resolved resolved, PickShape shape) {
        List<?> data;
        try {
            data = PickLocation.loadYaml(resolved, shape.row());
        } catch (RuntimeException e) {
            throw new IllegalStateException("[%s] pick %s failed to load %s".formatted(id, field.getName(), resolved.location()), e);
        }
        return data;
    }
}
