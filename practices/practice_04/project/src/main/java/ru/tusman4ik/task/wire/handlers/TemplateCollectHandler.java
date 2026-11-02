package ru.tusman4ik.task.wire.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.registry.TemplateId;
import ru.tusman4ik.task.registry.TemplateInfo;
import ru.tusman4ik.task.templates.annotations.TemplateDef;
import ru.tusman4ik.task.templates.Template;
import ru.tusman4ik.task.wire.Fields;
import ru.tusman4ik.task.wire.PrototypeHandler;
import ru.tusman4ik.task.wire.PrototypeInfos;

import java.lang.reflect.Field;

@Slf4j
public class TemplateCollectHandler implements PrototypeHandler {

    @Override
    public void handle(PrototypeId id, PrototypeInfo info) {
        Object generator = info.generator();
        int templates = 0;
        for (Field field : Fields.allOf(generator.getClass())) {
            TemplateDef def = field.getAnnotation(TemplateDef.class);
            if (def == null) {
                continue;
            }
            log.trace("[{}] template field enter: {}", id, field.getName());
            if (!Template.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("@TemplateDef on non-Template field " + field + " of " + id);
            }
            Object value = Fields.read(generator, field);
            if (value == null) {
                throw new IllegalStateException("@TemplateDef field not initialized " + field + " of " + id);
            }
            TemplateId templateId = new TemplateId(id, def.value());
            log.trace("[{}] template {} resolved: field={} templateId={}", id, def.value(), field.getName(), templateId);
            PrototypeInfos.putUnique(info.templates(), def.value(), new TemplateInfo(templateId, (Template) value), "template");
            templates++;
            log.debug("[{}] template {} <- {} templateId={}", id, def.value(), field.getName(), templateId);
        }
        log.debug("[{}] template-collect done: {} templates", id, templates);
    }
}
