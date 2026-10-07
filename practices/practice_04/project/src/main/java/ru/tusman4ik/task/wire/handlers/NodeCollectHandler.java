package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.nodes.annotations.NodeDef;
import ru.tusman4ik.task.nodes.annotations.PickBoolNodeDef;
import ru.tusman4ik.task.nodes.annotations.PickIntNodeDef;
import ru.tusman4ik.task.nodes.annotations.PickNodeDef;
import ru.tusman4ik.task.nodes.annotations.RetryNodeDef;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.wire.Fields;
import ru.tusman4ik.task.wire.PrototypeHandler;
import ru.tusman4ik.task.wire.PrototypeInfos;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

@Slf4j
public class NodeCollectHandler implements PrototypeHandler {

    private static final Set<Class<? extends Annotation>> SUPPORTED_TYPES = Set.of(
            NodeDef.class, PickNodeDef.class, PickBoolNodeDef.class, PickIntNodeDef.class, RetryNodeDef.class
    );

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        Object generator = info.generator();
        int nodes = 0;
        for (Field field : Fields.allOf(generator.getClass())) {

            if (SUPPORTED_TYPES.stream().noneMatch(field::isAnnotationPresent)) {
                continue;
            }
            String annotation = annotationNameOf(field);
            log.trace("[{}] node field enter: {} annotation={}", id, field.getName(), annotation);
            if (!Node.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("@" + annotation + " on non-Node field " + field + " of " + id);
            }
            Node<?> node = (Node<?>) Fields.read(generator, field);
            if (node == null) {
                throw new IllegalStateException("@" + annotation + " field not initialized " + field + " of " + id);
            }
            PrototypeInfos.putUnique(info.nodes(), field.getName(), node, "node");
            nodes++;
            log.trace("[{}] node {} collected: type={}", id, field.getName(), node.getClass().getSimpleName());
            log.debug("[{}] node {} <- {}", id, field.getName(), annotation);
        }
        log.debug("[{}] node-collect done: {} nodes", id, nodes);
    }

    private static String annotationNameOf(Field field) {
        for (Class<? extends Annotation> type : SUPPORTED_TYPES) {
            if (field.isAnnotationPresent(type)) {
                return type.getSimpleName();
            }
        }
        return "NodeDef";
    }
}
