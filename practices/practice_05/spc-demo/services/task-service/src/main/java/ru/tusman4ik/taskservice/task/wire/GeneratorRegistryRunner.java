package ru.tusman4ik.taskservice.task.wire;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeneratorRegistryRunner implements SmartInitializingSingleton {

    private final WireChain chain;
    private final GeneratorRegistry generatorRegistry;

    public GeneratorRegistryRunner(WireChain chain, GeneratorRegistry generatorRegistry) {
        this.chain = chain;
        this.generatorRegistry = generatorRegistry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        chain.runAll(generatorRegistry.getIds());
        log.debug("wire chain order: {}", chain.names());
    }
}
