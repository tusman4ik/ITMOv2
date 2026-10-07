package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.registry.TemplateInfo;
import ru.tusman4ik.task.wire.PrototypeHandler;

@Slf4j
public class ValidateHandler implements PrototypeHandler {

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        int rawRefs = 0;
        for (TemplateInfo template : info.templates().values()) {
            int templateRefs = 0;
            for (Node<?> raw : template.template().rawNodes()) {
                boolean known = false;
                for (Node<?> node : info.nodes().values()) {
                    if (node == raw) {
                        known = true;
                        break;
                    }
                }
                if (!known) {
                    throw new IllegalStateException("raw node not in nodes of " + id + ", template=" + template.id());
                }
                rawRefs++;
                templateRefs++;
            }
            log.trace("[{}] validated {}: {} raw refs", id, template.id(), templateRefs);
        }
        log.debug("[{}] validated {} templates, {} raw refs", id, info.templates().size(), rawRefs);
    }
}
