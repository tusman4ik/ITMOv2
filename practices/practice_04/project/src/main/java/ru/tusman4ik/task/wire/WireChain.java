package ru.tusman4ik.task.wire;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public final class WireChain {

    private final List<PrototypeHandler> handlers;

    private WireChain(List<PrototypeHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public static WireChain of(PrototypeHandler first, PrototypeHandler... rest) {
        List<PrototypeHandler> list = new ArrayList<>();
        list.add(first);
        list.addAll(List.of(rest));
        return new WireChain(list);
    }

    public WireChain andAfter(PrototypeHandler next) {
        List<PrototypeHandler> list = new ArrayList<>(handlers);
        list.add(next);
        return new WireChain(list);
    }

    public void runAll(Map<PrototypeId, PrototypeInfo> infos) {
        for (PrototypeHandler handler : handlers) {
            String name = handler.getClass().getSimpleName();
            long start = System.nanoTime();
            for (PrototypeInfo info : infos.values()) {
                try {
                    handler.handle(info.id(), info);
                } catch (RuntimeException e) {
                    throw new IllegalStateException("handler " + name + " failed on " + info.id(), e);
                }
            }
            log.debug("handler {} done in {}ms", name, (System.nanoTime() - start) / 1_000_000);
        }
    }

    public List<String> names() {
        return handlers.stream().map(h -> h.getClass().getSimpleName()).toList();
    }
}
