package ru.tusman4ik.taskservice.task.wire;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class WireChain {

    private final List<PrototypeHandler> handlers;

    private WireChain(List<PrototypeHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public static WireChain of(@NonNull PrototypeHandler first) {
        List<PrototypeHandler> list = new ArrayList<>();
        list.add(first);
        return new WireChain(list);
    }

    public WireChain andAfter(@NonNull PrototypeHandler next) {
        List<PrototypeHandler> list = new ArrayList<>(handlers);
        list.add(next);
        return new WireChain(list);
    }

    public void runAll(Set<PrototypeId> ids) {
        log.debug("{} handlers discovered", handlers.size());
        for (PrototypeHandler handler : handlers) {
            String name = handler.getClass().getSimpleName();
            log.debug("----------------- ({} started) -----------------", name);
            long start = System.nanoTime();
            ids.forEach(
                    k -> {
                        try {
                            log.debug("For {}:", k);
                            handler.handle(k);
                        } catch (RuntimeException e) {
                            throw new IllegalStateException("handler " + name + " failed on " + k, e);
                        }
                    }
            );

            log.debug("{} done in {}ms", name, (System.nanoTime() - start) / 1_000_000);
        }
    }

    public List<String> names() {
        return handlers.stream().map(h -> h.getClass().getSimpleName()).toList();
    }

}
