package ru.tusman4ik.task.templates;

import ru.tusman4ik.task.context.FrozenContext;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.resources.TemplateResources;

import java.util.Optional;


public record CallCtx(FrozenContext params, TemplateResources res) {

    public <T> T get(Node<T> node) {
        return params.get(node);
    }

    public <T> Optional<T> getOpt(Node<T> node) {
        return params.getOpt(node);
    }
}
