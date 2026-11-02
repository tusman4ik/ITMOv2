package ru.tusman4ik.taskservice.task.wire;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;
import ru.tusman4ik.apicontracts.openapi.PolymorphicEntityIndex;
import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.generator.Register;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class GeneratorRegistryInitializer {

    private final ApplicationContext context;

    public GeneratorRegistryInitializer(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public GeneratorRegistry generatorRegistry() {
        Map<PrototypeId, PrototypeGenerator<?>> generators = new HashMap<>();
        for (Object bean : context.getBeansWithAnnotation(Register.class).values()) {
            PrototypeGenerator<?> generator = tryExtractGenerator(bean);

            Class<?> targetClass = AopUtils.getTargetClass(generator);
            Register annotation = tryExtractAnnotation(targetClass);

            PrototypeId id = toId(annotation, targetClass);
            PrototypeGenerator<?> previous = generators.putIfAbsent(id, generator);
            throwIfAlreadyRegistered(previous, id, targetClass);
        }
        return new GeneratorRegistry(generators);
    }

    @Bean
    public SchemaRegistry schemaRegistry() {
        Map<PrototypeId, Class<? extends InputSchema>> schemas = new HashMap<>();
        for (Object bean : context.getBeansWithAnnotation(Register.class).values()) {
            PrototypeGenerator<?> generator = tryExtractGenerator(bean);

            Class<?> targetClass = AopUtils.getTargetClass(generator);
            Register annotation = tryExtractAnnotation(targetClass);

            PrototypeId id = toId(annotation, targetClass);
            schemas.put(id, PolymorphicEntityIndex.relatedInputSchema(generator.form().getClass()));
        }
        return new SchemaRegistry(schemas);
    }


    private static @NonNull Register tryExtractAnnotation(Class<?> targetClass) {
        Register annotation = AnnotationUtils.findAnnotation(targetClass, Register.class);
        if (annotation == null) {
            throw new IllegalStateException(
                    "@Register annotation not found on " + targetClass.getName());
        }
        return annotation;
    }

    private static @NonNull PrototypeGenerator<?> tryExtractGenerator(Object bean) {
        if (!(bean instanceof PrototypeGenerator<?> generator)) {
            throw new IllegalStateException(
                    "@Register bean must extend PrototypeGenerator: " + bean.getClass().getName());
        }
        return generator;
    }

    private static PrototypeId toId(Register register, Class<?> beanClass) {
        try {
            return new PrototypeId(register.exam(), register.subject(), register.number(), register.prototypeIdx());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalStateException("Invalid @Register on " + beanClass.getName() + ": " + e.getMessage(), e);
        }
    }

    private static void throwIfAlreadyRegistered(PrototypeGenerator<?> previous, PrototypeId id, Class<?> targetClass) {
        if (previous != null) {
            throw new IllegalStateException("Duplicate prototype " + id + ": "
                    + previous.getClass().getName() + " and " + targetClass.getName());
        }
    }

}
