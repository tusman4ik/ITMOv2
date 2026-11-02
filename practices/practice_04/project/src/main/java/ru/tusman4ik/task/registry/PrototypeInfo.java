package ru.tusman4ik.task.registry;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import ru.tusman4ik.task.context.Context;
import ru.tusman4ik.task.context.FrozenContext;
import ru.tusman4ik.task.generators.PrototypeGenerator;
import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.templates.CheckResult;
import ru.tusman4ik.task.templates.Template;
import ru.tusman4ik.task.ui.GeneratedTask;
import ru.tusman4ik.task.ui.forms.AnswerForm;
import ru.tusman4ik.task.ui.forms.FormDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public record PrototypeInfo(
        PrototypeId id,
        PrototypeGenerator generator,
        Map<String, Node<?>> nodes,
        List<Node<?>> nodesInLexicographicalOrder,
        Map<Integer, TemplateInfo> templates,
        List<TemplateInfo> templatesInOrder
) {

    public static @NonNull PrototypeInfo of(PrototypeGenerator generator, PrototypeId id) {
        return new PrototypeInfo(id, generator,
                new LinkedHashMap<>(),
                new ArrayList<>(),
                new LinkedHashMap<>(),
                new ArrayList<>());
    }

    public GeneratedTask produce(long seed) {
        log.debug("Start producing for {}. seed: {}", id, seed);

        try (Session session = open(seed)) {
            log.trace("[{}] scope opened: seed={}", id, seed);
            TemplateInfo selected = prepare(session);
            Template template = selected.template();
            FrozenContext ctx = session.context.freeze();

            log.trace("[{}] params generated: warmed={}, frozen={}", id,
                    nodesInLexicographicalOrder.size(), ctx.size());
            if (!nodesInLexicographicalOrder.isEmpty() && ctx.size() == 0) {
                throw new IllegalStateException("params not generated for " + id + " seed=" + seed);
            }

            String statement = template.render(ctx, nodes);

            log.debug("[{}] statement rendered (seed={}): {}", id, seed, statement);
            Map<String, Object> data = resolveRaw(template, ctx);
            AnswerForm<?> form = template.form(ctx);
            FormDescriptor inputForm = form.descriptor();
            log.trace("[{}] task assembled: rawValues={}", id, data.size());
            return new GeneratedTask(statement, data, inputForm);
        }
    }


    public CheckResult check(long seed, String json) {
        if (json == null) {
            throw new IllegalArgumentException("check json must be not null of " + id + " seed=" + seed);
        }
        log.debug("Start checking for {}. seed: {}", id, seed);
        try (Session session = open(seed)) {
            TemplateInfo selected = prepare(session);
            FrozenContext ctx = session.context.freeze();
            CheckResult result = selected.template().check(ctx, json);
            log.trace("[{}] checked (seed={}): criteria={} total={}", id, seed,
                    result.criteria().size(), result.total());
            return result;
        }
    }

    private TemplateInfo prepare(Session session) {
        nodesInLexicographicalOrder.forEach(Node::get);
        if (templatesInOrder.isEmpty()) {
            throw new IllegalStateException("no templates wired for " + id);
        }
        int templateIdx = session.context.random().nextInt(templatesInOrder.size());
        TemplateInfo selected = templatesInOrder().get(templateIdx);
        log.trace("[{}] prepared: warmed={} nodes, templateIdx={} templateId={}", id,
                nodesInLexicographicalOrder.size(), templateIdx, selected.id());
        return selected;
    }

    private Session open(long seed) {
        Context ctx = new Context(seed);
        return new Session(ctx, Context.replace(ctx));
    }

    /**
     * Значения raw-нод по их именам. Raw-узел обязан быть среди нод прототипа
     * (проверяется {@code ValidateHandler} при старте); сопоставление по identity,
     * т.к. снапшот ключуется мемоизированными обёртками. Null-значения
     * (стабы вроде {@code matrix}) пропускаются.
     */
    private Map<String, Object> resolveRaw(Template template, FrozenContext ctx) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (Node<?> raw : template.rawNodes()) {
            String name = null;
            for (Map.Entry<String, Node<?>> entry : nodes.entrySet()) {
                if (entry.getValue() == raw) {
                    name = entry.getKey();
                    break;
                }
            }
            if (name == null) {
                throw new IllegalStateException("raw node not in nodes of " + id);
            }
            Object value = ctx.snapshot().get(raw);
            if (value != null) {
                data.put(name, value);
            }
        }
        return data;
    }

    private record Session(Context context, Context previous) implements AutoCloseable {

        @Override
        public void close() {
            Context.restore(previous);
        }
    }
}
