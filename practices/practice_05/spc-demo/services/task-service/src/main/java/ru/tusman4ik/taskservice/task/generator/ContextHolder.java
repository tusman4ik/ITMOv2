package ru.tusman4ik.taskservice.task.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tusman4ik.apicontracts.taskservice.check.CheckResult;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;
import ru.tusman4ik.apicontracts.taskservice.task.TaskDto;
import ru.tusman4ik.taskservice.common.error.DomainException;
import ru.tusman4ik.taskservice.common.error.implementations.ContextNotBoundException;
import ru.tusman4ik.taskservice.common.error.implementations.NodeWarmUpException;
import ru.tusman4ik.taskservice.task.id.AttemptId;
import ru.tusman4ik.taskservice.task.id.InstanceId;
import ru.tusman4ik.taskservice.task.wire.GeneratorRegistry;
import ru.tusman4ik.taskservice.task.wire.SchemaRegistry;
import ru.tusman4ik.taskservice.task.wire.implementations.FingerprintCollector;
import ru.tusman4ik.taskservice.task.wire.implementations.TemplateHandler;
import ru.tusman4ik.taskservice.task.wire.implementations.models.NodeFingerprint;

import java.util.List;
import java.util.TreeSet;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ContextHolder {

    final static ScopedValue<Context> CONTEXT = ScopedValue.newInstance();

    private final GeneratorRegistry generatorRegistry;
    private final FingerprintCollector fingerprintCollector;
    private final TemplateHandler templateHandler;
    private final SchemaRegistry schemaRegistry;

    public static Context current() {
        if (!CONTEXT.isBound()) {
            throw new ContextNotBoundException();
        }
        return CONTEXT.get();
    }

    public TaskDto generate(InstanceId id) {
        return ScopedValue.where(CONTEXT, new Context(id))
                .call(() -> {
                    FrozenContext frozen = warm(id);
                    PrototypeGenerator<?> generator = generatorRegistry.getGenerator(id.prototypeId());
                    String statement = templateHandler.render(id.prototypeId(), frozen);
                    return new TaskDto(id.toDto(), statement, List.of(), generator.form());
                });
    }

    public CheckResult check(AttemptId id, InputSchema schema) {
        schemaRegistry.require(id.prototypeId(), schema);
        return ScopedValue.where(CONTEXT, new Context(id.instanceId()))
                .call(() -> {
                    warm(id.instanceId());

                    @SuppressWarnings("unchecked")
                    PrototypeGenerator<InputSchema> generator = (PrototypeGenerator<InputSchema>) generatorRegistry.getGenerator(id.prototypeId());
                    return generator.check(id, schema);
                });
    }

    public <T> T warmed(InstanceId id, Supplier<T> action) {
        return ScopedValue.where(CONTEXT, new Context(id))
                .call(() -> {
                    warm(id);
                    return action.get();
                });
    }

    private FrozenContext warm(InstanceId id) {
        TreeSet<NodeFingerprint> nodes = fingerprintCollector.getNodes(id.prototypeId());

        for (NodeFingerprint fingerprint : nodes) {
            try {
                fingerprint.node().get();
            } catch (DomainException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new NodeWarmUpException(id, "cannot warm node " + fingerprint.shema().name(), e);
            }
        }

        FrozenContext frozen = CONTEXT.get().freeze();

        if (!nodes.isEmpty() && frozen.size() != nodes.size()) {
            throw new NodeWarmUpException(
                    id, "not all nodes warmed: %d of %d".formatted(frozen.size(), nodes.size()));
        }
        return frozen;
    }
}
