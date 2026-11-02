package ru.tusman4ik.task.resources;

import ru.tusman4ik.task.registry.TemplateId;

public record TemplateResources(TemplateId id, ResourceResolver proto, ResourceResolver tmpl) {
}
