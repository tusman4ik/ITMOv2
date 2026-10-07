package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.wire.PrototypeHandler;

import java.util.Map;

@Slf4j
public class NodeNameRegisterHandler implements PrototypeHandler {

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        info.nodesInLexicographicalOrder().addAll(
                info.nodes().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .toList()
        );

        info.templatesInOrder().addAll(
                info.templates().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .toList()
        );
        log.trace("[{}] node order: {}", id, info.nodes().keySet().stream().sorted().toList());
        log.trace("[{}] template order: {}", id, info.templates().keySet().stream().sorted().toList());
        log.debug("[{}] registered {} nodes, {} templates in order",
                id, info.nodesInLexicographicalOrder().size(), info.templatesInOrder().size());
    }
}
