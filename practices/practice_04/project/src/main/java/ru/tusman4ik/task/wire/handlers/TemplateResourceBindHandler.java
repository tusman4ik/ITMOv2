package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.registry.TemplateId;
import ru.tusman4ik.task.registry.TemplateInfo;
import ru.tusman4ik.task.resources.Loader;
import ru.tusman4ik.task.resources.ResourceResolver;
import ru.tusman4ik.task.resources.TemplateResources;
import ru.tusman4ik.task.wire.Fields;
import ru.tusman4ik.task.wire.PrototypeHandler;

@Slf4j
public class TemplateResourceBindHandler implements PrototypeHandler {

    private final Loader loader;

    public TemplateResourceBindHandler(Loader loader) {
        this.loader = loader;
    }

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        ResourceResolver proto = ResourceResolver.root(loader, id.path());
        int bound = 0;
        for (TemplateInfo template : info.templates().values()) {
            TemplateId templateId = template.id();
            ResourceResolver tmpl = ResourceResolver.root(loader, templateId.path());
            log.trace("[{}] resource bind enter: templateId={} protoBase={} tmplBase={}",
                    id, templateId, proto.base(), tmpl.base());
            Fields.inject(template.template(), "resources", new TemplateResources(templateId, proto, tmpl));
            bound++;
            log.debug("[{}] resources bound for {} proto={} tmpl={}", id, templateId, proto.base(), tmpl.base());
        }
        log.debug("[{}] resource-bind done: {} templates", id, bound);
    }
}
