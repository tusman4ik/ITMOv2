package ru.tusman4ik.taskservice.task.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.tusman4ik.apicontracts.taskservice.check.SuccessCheck;
import ru.tusman4ik.apicontracts.taskservice.check.CheckedItemDto;
import ru.tusman4ik.apicontracts.taskservice.check.CheckRequestDto;
import ru.tusman4ik.apicontracts.taskservice.check.CheckResponseDto;
import ru.tusman4ik.apicontracts.taskservice.check.CheckItemDto;
import ru.tusman4ik.apicontracts.taskservice.task.CreatePrototypeOrderDto;
import ru.tusman4ik.apicontracts.taskservice.task.GenerateRequestDto;
import ru.tusman4ik.apicontracts.taskservice.task.RecreateRequestDto;
import ru.tusman4ik.apicontracts.taskservice.task.TaskDto;
import ru.tusman4ik.apicontracts.taskservice.task.TasksResponseDto;
import ru.tusman4ik.taskservice.task.generator.ContextHolder;
import ru.tusman4ik.taskservice.task.id.AttemptId;
import ru.tusman4ik.taskservice.task.id.InstanceId;
import ru.tusman4ik.taskservice.task.id.PrototypeId;
import ru.tusman4ik.apicontracts.taskservice.api.TaskApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class TaskController implements TaskApi {

    private static final long FINGERPRINT_PLACEHOLDER = 0L;

    private final ContextHolder contextHolder;
    private final ru.tusman4ik.taskservice.external.coderunner.InMemoryVerdictStore verdictStore;

    public TaskController(ContextHolder contextHolder,
                          ru.tusman4ik.taskservice.external.coderunner.InMemoryVerdictStore verdictStore) {
        this.contextHolder = contextHolder;
        this.verdictStore = verdictStore;
    }

    private static final long MAX_SAFE_JS_INTEGER = 9007199254740991L; // 2^53 - 1

    @Override
    public TasksResponseDto generate(@Valid @RequestBody GenerateRequestDto req) {
        List<TaskDto> tasks = new ArrayList<>();
        for (CreatePrototypeOrderDto order : req.items()) {
            PrototypeId prototypeId = PrototypeId.from(order.prototype());
            for (int i = 0; i < order.count(); i++) {
                long seed = ThreadLocalRandom.current().nextLong(1, MAX_SAFE_JS_INTEGER);
                tasks.add(contextHolder.generate(prototypeId.getInstance(seed, FINGERPRINT_PLACEHOLDER)));
            }
        }
        return new TasksResponseDto(tasks);
    }

    @Override
    public TasksResponseDto recreate(@Valid @RequestBody RecreateRequestDto req) {
        List<TaskDto> tasks = req.items().stream()
                .map(item -> contextHolder.generate(InstanceId.from(item)))
                .toList();
        return new TasksResponseDto(tasks);
    }

    @Override
    public CheckResponseDto check(@Valid @RequestBody CheckRequestDto req) {
        List<CheckedItemDto> items = new ArrayList<>();
        for (CheckItemDto item : req.items()) {
            AttemptId attemptId = AttemptId.from(item.attempt());
            SuccessCheck cached = verdictStore.get(attemptId);
            if (cached != null) {
                items.add(new CheckedItemDto(attemptId.toDto(), cached));
            } else {
                items.add(new CheckedItemDto(attemptId.toDto(), contextHolder.check(attemptId, item.answer())));
            }
        }
        return new CheckResponseDto(items);
    }
}
