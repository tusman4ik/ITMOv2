package ru.tusman4ik.task.wire;

import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;

public interface PrototypeHandler {
    void handle(PrototypeId id, PrototypeInfo info);
}
