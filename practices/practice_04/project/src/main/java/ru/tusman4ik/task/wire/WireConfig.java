package ru.tusman4ik.task.wire;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tusman4ik.task.resources.Loader;
import ru.tusman4ik.task.wire.handlers.*;

@Configuration
public class WireConfig {

    @Bean
    public PickNodeCollectHandler pickNodeCollectHandler(Loader loader) {
        return new PickNodeCollectHandler(loader);
    }

    @Bean
    public PickBoolNodeCollectHandler pickBoolNodeCollectHandler() {
        return new PickBoolNodeCollectHandler();
    }

    @Bean
    public PickIntNodeCollectHandler pickIntNodeCollectHandler() {
        return new PickIntNodeCollectHandler();
    }

    @Bean
    public RetryNodeCollectHandler retryNodeCollectHandler() {
        return new RetryNodeCollectHandler();
    }

    @Bean
    public TemplateCollectHandler templateCollectHandler() {
        return new TemplateCollectHandler();
    }

    @Bean
    public NodeCollectHandler nodeCollectHandler() {
        return new NodeCollectHandler();
    }

    @Bean
    public TemplateResourceBindHandler templateResourceBindHandler(Loader loader) {
        return new TemplateResourceBindHandler(loader);
    }

    @Bean
    public ValidateHandler validateHandler() {
        return new ValidateHandler();
    }

    @Bean
    public MemoizeHandler memoizeHandler() {
        return new MemoizeHandler();
    }

    @Bean
    public NodeNameRegisterHandler nodeNameRegisterHandler() {
        return new NodeNameRegisterHandler();
    }

    @Bean
    public WireChain chain(Loader loader) {
        return WireChain
                .of(pickNodeCollectHandler(loader))
                .andAfter(pickBoolNodeCollectHandler())
                .andAfter(pickIntNodeCollectHandler())
                .andAfter(retryNodeCollectHandler())
                .andAfter(templateCollectHandler())
                .andAfter(nodeCollectHandler())
                .andAfter(templateResourceBindHandler(loader))
                .andAfter(validateHandler())
                .andAfter(memoizeHandler())
                .andAfter(nodeNameRegisterHandler());
    }
}
