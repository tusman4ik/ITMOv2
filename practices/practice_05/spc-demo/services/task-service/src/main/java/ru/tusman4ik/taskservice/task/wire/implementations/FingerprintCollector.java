package ru.tusman4ik.taskservice.task.wire.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.id.PrototypeId;
import ru.tusman4ik.taskservice.task.node.Node;
import ru.tusman4ik.taskservice.task.wire.GeneratorRegistry;
import ru.tusman4ik.taskservice.task.wire.PrototypeHandler;
import ru.tusman4ik.taskservice.common.error.implementations.UnknownPrototypeException;
import ru.tusman4ik.taskservice.task.wire.implementations.models.NodeFingerprint;
import ru.tusman4ik.taskservice.task.wire.implementations.models.NodeSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@Slf4j
@Component
public class FingerprintCollector implements PrototypeHandler {

    private final NodeScanner nodeScanner;
    private final GeneratorRegistry generatorRegistry;
    private final Map<PrototypeId, TreeSet<NodeFingerprint>> nodeFingerprints = new HashMap<>();

    public FingerprintCollector(NodeScanner nodeScanner, GeneratorRegistry generatorRegistry) {
        this.nodeScanner = nodeScanner;
        this.generatorRegistry = generatorRegistry;
    }

    @Override
    public void handle(PrototypeId id) {
        nodeFingerprints.computeIfAbsent(id, _ -> new TreeSet<>());
        PrototypeGenerator<?> generator = generatorRegistry.getGenerator(id);
        nodeScanner.getNodes(id)
                .forEach(schema -> registerNode(id, schema, generator));
    }

    public TreeSet<NodeFingerprint> getNodes(PrototypeId id) {
        TreeSet<NodeFingerprint> nodes = nodeFingerprints.get(id);
        if (nodes == null) {
            throw new UnknownPrototypeException(id);
        }
        return nodes;
    }

    private void registerNode(PrototypeId id, NodeSchema schema, PrototypeGenerator<?> generator) {
        try {
            var fingerprint = new NodeFingerprint(schema, (Node<?>) schema.node().get(generator));
            nodeFingerprints.get(id).add(fingerprint);
            log.trace("  - {}, hash: {}", schema.name(), System.identityHashCode(fingerprint.node()));
        } catch (IllegalAccessException e) {
            log.error("Access to the node '{}' is not possible. Try calling Field::setAccessible(true)", schema.name());
            throw new RuntimeException(e);
        }
    }
}
