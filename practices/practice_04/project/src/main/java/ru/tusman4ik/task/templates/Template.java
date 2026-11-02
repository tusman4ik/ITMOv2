package ru.tusman4ik.task.templates;

import ru.tusman4ik.task.context.FrozenContext;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.resources.TemplateResources;
import ru.tusman4ik.task.ui.forms.AnswerForm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class Template {

    @SuppressWarnings("unused") // will be injected in registry
    private TemplateResources resources;

    private final UiBox ui;
    private final List<Supplier<Node<?>>> rawNodes = new ArrayList<>();
    private RenderEngine renderEngine = DefaultRenderEngineFactory.get();

    private Template(UiBox ui) {
        this.ui = ui;
    }

    public static <I> Template of(FormFactory<I> form, CheckerFactory<I> checker) {
        return new Template(new UiBox.Impl<>(form, checker));
    }

    public static Template of(UiBox box) {
        return new Template(box);
    }

    @SafeVarargs
    public final Template raw(Supplier<Node<?>>... refs) {
        rawNodes.addAll(List.of(refs));
        return this;
    }

    public Template renderBy(RenderEngine engine) {
        renderEngine = engine;
        return this;
    }

    public String render(FrozenContext params, Map<String, Node<?>> nodes) {

        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, Node<?>> e : nodes.entrySet()) {
            if (!params.snapshot().containsKey(e.getValue())) {
                throw new IllegalStateException("Parameter " + e.getKey() + " was not warmed");
            }
            Object value = params.snapshot().get(e.getValue());
            if (value != null) {
                values.put(e.getKey(), value);
            }
        }

        CallCtx ctx = call(params);
        return renderEngine.getStatement(ctx, values);
    }

    public AnswerForm<?> form(FrozenContext params) {
        return ui.form(call(params));
    }

    public CheckResult check(FrozenContext params, String json) {
        return ui.check(call(params), json);
    }

    public List<Node<?>> rawNodes() {
        return rawNodes.stream()
                .<Node<?>>map(Supplier::get)
                .toList();
    }

    private CallCtx call(FrozenContext params) {
        if (resources == null) {
            throw new IllegalStateException("template not bound: bindResources missing");
        }
        return new CallCtx(params, resources);
    }
}
