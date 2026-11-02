package ru.tusman4ik.task.registry;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import ru.tusman4ik.task.generators.PrototypeGenerator;
import ru.tusman4ik.task.wire.WireChain;
import ru.tusman4ik.task.wire.PrototypeInfos;
import ru.tusman4ik.task.registry.annotations.Register;

import java.util.*;

@Slf4j
@Component
public class GeneratorRegistry {

    private final Map<PrototypeId, PrototypeInfo> infos = new LinkedHashMap<>();
    private final ApplicationContext context;
    private final WireChain chain;

    public GeneratorRegistry(ApplicationContext context, WireChain chain) {
        this.context = context;
        this.chain = chain;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> beans = context.getBeansWithAnnotation(Register.class);
        for (Object bean : beans.values()) {
            if (!(bean instanceof PrototypeGenerator generator)) {
                throw new IllegalStateException(
                        "@Register bean must extend PrototypeGenerator: " + bean.getClass().getName());
            }
            Class<?> targetClass = AopUtils.getTargetClass(generator);
            Register annotation = AnnotationUtils.findAnnotation(targetClass, Register.class);
            if (annotation == null) {
                throw new IllegalStateException(
                        "@Register annotation not found on " + targetClass.getName());
            }
            PrototypeId id = PrototypeId.from(annotation);

            PrototypeInfos.putUnique(infos, id,
                    PrototypeInfo.of(generator, id), "prototype");
        }
        chain.runAll(infos);
        log.debug("wire chain order: {}", chain.names());
    }



    public Map<PrototypeId, PrototypeInfo> infos() {
        return Collections.unmodifiableMap(infos);
    }

    public PrototypeInfo info(PrototypeId id) {
        PrototypeInfo info = infos.get(id);
        if (info == null) {
            throw new IllegalArgumentException("unknown prototype " + id);
        }
        return info;
    }
}
