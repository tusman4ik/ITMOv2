package ru.tusman4ik.taskservice.task.wire.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.id.PrototypeId;
import ru.tusman4ik.taskservice.task.node.Node;
import ru.tusman4ik.taskservice.task.node.Nodes;
import ru.tusman4ik.taskservice.task.wire.GeneratorRegistry;
import ru.tusman4ik.taskservice.task.wire.PrototypeHandler;
import ru.tusman4ik.taskservice.task.wire.implementations.models.NodeSchema;

import java.util.TreeSet;

@Slf4j
@Component
public class MemoizeHandler implements PrototypeHandler {

    private final NodeScanner nodeScanner;
    private final GeneratorRegistry generatorRegistry;

    public MemoizeHandler(NodeScanner nodeScanner, GeneratorRegistry generatorRegistry) {
        this.nodeScanner = nodeScanner;
        this.generatorRegistry = generatorRegistry;
    }

    @Override
    public void handle(PrototypeId id) {
        TreeSet<NodeSchema> nodes = nodeScanner.getNodes(id);
        PrototypeGenerator<?> generator = generatorRegistry.getGenerator(id);

        int wrapped = 0;
        int skipped = 0;
        for (NodeSchema schema : nodes) {
            if (memoizeNode(id, schema, generator)) {
                wrapped++;
            } else {
                skipped++;
            }
        }
        log.debug("{}: memoized {} nodes ({} already memoized)", id, wrapped, skipped);
    }

    private boolean memoizeNode(PrototypeId id, NodeSchema schema, PrototypeGenerator<?> generator) {
        try {
            Node<?> node = (Node<?>) schema.node().get(generator);

            if (node == null) {
                throw new IllegalStateException(
                        "node field %s of %s is not initialized; use field initializer"
                                .formatted(schema.node(), id));
            }
            if (node instanceof Nodes.Memoized<?>) {
                log.trace("  - {}: already memoized, skipped", schema.name());
                return false;
            }

            schema.node().set(generator, Nodes.memoized(node));
            log.trace("  - {}: wrapped", schema.name());
            return true;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "cannot memoize node field %s of %s".formatted(schema.node(), id), e);
        }
    }
}
