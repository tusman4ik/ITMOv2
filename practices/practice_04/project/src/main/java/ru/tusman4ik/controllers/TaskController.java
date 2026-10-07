package ru.tusman4ik.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tusman4ik.task.registry.GeneratorRegistry;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.templates.CheckResult;
import ru.tusman4ik.task.ui.GeneratedTask;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@RequestMapping("/v1/tasks")
public class TaskController {

    private final GeneratorRegistry registry;

    public TaskController(GeneratorRegistry registry) {this.registry = registry;}

    @PostMapping("gen")
    public GenTaskResponse gen(@RequestBody @Valid GenRequest request) {
        long seed = request.seed() != null ? request.seed() : ThreadLocalRandom.current().nextLong();
        PrototypeInfo info = Prototypes.lookup(registry, request.id());
        GeneratedTask task = info.produce(seed);
        log.debug("generated {} seed={}", info.id(), seed);
        return new GenTaskResponse(request.id(), seed,
                task.statement(), task.data(), task.inputForm());
    }

    @PostMapping("check")
    public CheckResult check(@RequestBody @Valid CheckRequest request) {
        PrototypeInfo info = Prototypes.lookup(registry, request.id());
        return info.check(request.seed(), request.answer().toString());
    }
}
