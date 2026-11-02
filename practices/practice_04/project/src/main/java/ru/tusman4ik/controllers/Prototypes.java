package ru.tusman4ik.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.tusman4ik.task.registry.GeneratorRegistry;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;

final class Prototypes {

    private Prototypes() {
    }

    static PrototypeInfo lookup(GeneratorRegistry registry, PrototypeId id) {
        PrototypeInfo info = registry.infos().get(id);
        if (info == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown prototype " + id);
        }
        return info;
    }
}
