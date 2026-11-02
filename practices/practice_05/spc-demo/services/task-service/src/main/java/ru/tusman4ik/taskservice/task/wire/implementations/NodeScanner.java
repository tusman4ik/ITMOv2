package ru.tusman4ik.taskservice.task.wire.implementations;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import ru.tusman4ik.taskservice.task.id.PrototypeId;
import ru.tusman4ik.taskservice.task.node.Node;
import ru.tusman4ik.taskservice.task.node.NodeDefMarkerScanner;
import ru.tusman4ik.taskservice.task.wire.GeneratorRegistry;
import ru.tusman4ik.taskservice.task.wire.PrototypeHandler;
import ru.tusman4ik.taskservice.task.wire.implementations.models.NodeSchema;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NodeScanner implements PrototypeHandler {

    private final Map<PrototypeId, TreeSet<NodeSchema>> nodeRegistry = new HashMap<>();
    private final GeneratorRegistry generatorRegistry;
    private final NodeDefMarkerScanner nodeDefMarkerScanner;

    public NodeScanner(GeneratorRegistry generatorRegistry, NodeDefMarkerScanner nodeDefMarkerScanner) {
        this.generatorRegistry = generatorRegistry;
        this.nodeDefMarkerScanner = nodeDefMarkerScanner;
    }

    @Override
    public void handle(PrototypeId id) {
        nodeRegistry.computeIfAbsent(id, _ -> new TreeSet<>());
        var fields = Arrays.stream(generatorRegistry
                        .getGeneratorClass(id)
                        .getDeclaredFields())
                .filter(f -> Node.class.isAssignableFrom(f.getType()))
                .toList();

        for (Field field : fields) {
            List<Annotation> nodeAnnotations = getNodeAnnotations(field);
            field.setAccessible(true);

            Annotation annotation = tryExtractAnnotation(field, nodeAnnotations);

            NodeSchema schema = NodeSchema.builder()
                    .annotation(annotation)
                    .name(field.getName())
                    .node(field)
                    .build();

            nodeRegistry.get(id).add(schema);
            log.trace("  - {} (@{}) registered", schema.name(), schema.annotation().annotationType().getSimpleName());
        }

    }

    public TreeSet<NodeSchema> getNodesAnnotatedBy(PrototypeId id, Class<? extends Annotation> annotation) {
        if (!nodeRegistry.containsKey(id)) {
            throw new IllegalArgumentException("Id have not registered yet: " + id);
        }
        return nodeRegistry.get(id)
                .stream()
                .filter(t -> t.annotation().annotationType().equals(annotation))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public TreeSet<NodeSchema> getNodes(PrototypeId id) {
        if (!nodeRegistry.containsKey(id)) {
            throw new IllegalArgumentException("Id have not registered yet: " + id);
        }
        return nodeRegistry.get(id);
    }

    private static Annotation tryExtractAnnotation(Field field, List<Annotation> nodeAnnotations) {
        if (nodeAnnotations.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one node annotation on field %s, found %d: %s"
                            .formatted(
                                    field.toGenericString(),
                                    nodeAnnotations.size(),
                                    nodeAnnotations.stream()
                                            .map(a -> "@" + a.annotationType().getSimpleName())
                                            .toList()
                            )
            );
        }
        return nodeAnnotations.getFirst();
    }


    private @NonNull List<Annotation> getNodeAnnotations(Field field) {
        return Arrays.stream(field.getAnnotations())
                .filter(a -> nodeDefMarkerScanner.contains(a.annotationType()))
                .toList();
    }
}
