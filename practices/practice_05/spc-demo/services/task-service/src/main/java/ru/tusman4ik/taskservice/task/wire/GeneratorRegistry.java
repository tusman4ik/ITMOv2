package ru.tusman4ik.taskservice.task.wire;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import ru.tusman4ik.taskservice.common.error.implementations.UnknownPrototypeException;
import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
public record GeneratorRegistry(
        Map<PrototypeId, PrototypeGenerator<?>> generators
) {

    @Override
    public Map<PrototypeId, PrototypeGenerator<?>> generators() {
        return Collections.unmodifiableMap(generators);
    }

    public PrototypeGenerator<?> getGenerator(PrototypeId id) {
        PrototypeGenerator<?> generator = generators.get(id);
        if (generator == null) {
            throw new UnknownPrototypeException(id);
        }
        return generator;
    }

    public Set<PrototypeId> getIds() {
        return generators.keySet();
    }

    public Class<? extends PrototypeGenerator> getGeneratorClass(PrototypeId id) {
        return AopUtils.getTargetClass(getGenerator(id)).asSubclass(PrototypeGenerator.class);
    }

    public boolean contains(PrototypeId id) {return generators.containsKey(id);}

    private static void throwIfAlreadyRegistered(PrototypeGenerator<?> previous, PrototypeId id, Class<?> targetClass) {
        if (previous != null) {
            throw new IllegalStateException("Duplicate prototype " + id + ": "
                    + previous.getClass().getName() + " and " + targetClass.getName());
        }
    }

}
