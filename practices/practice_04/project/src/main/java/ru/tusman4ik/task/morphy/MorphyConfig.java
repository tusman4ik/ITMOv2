package ru.tusman4ik.task.morphy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class MorphyConfig {

    @Bean
    public PendingStore pendingStore(@Value("${morphy.pending-dir:build/morphy-pending}") String pendingDir) {
        return new PendingStore(Path.of(pendingDir));
    }

    @Bean
    public MorphyService morphyService(AllowedFormsStore store, PendingStore pending) {
        return new MorphyService(store.loadAllowed(), pending, store.allowedLocation());
    }
}
