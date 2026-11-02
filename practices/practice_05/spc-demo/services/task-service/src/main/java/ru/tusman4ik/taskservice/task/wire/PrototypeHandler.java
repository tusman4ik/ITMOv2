package ru.tusman4ik.taskservice.task.wire;

import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

public interface PrototypeHandler {
    void handle(PrototypeId id);
}
