package ru.tusman4ik.task.resources.pick;

import ru.tusman4ik.task.nodes.annotations.PickNodeDef;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.resources.Loader;
import ru.tusman4ik.task.resources.ResourceResolver;

import java.util.List;

public final class PickLocation {

    private PickLocation() {}

    public record Resolved(ResourceResolver resolver, String file, String location, String strategy) {}

    public static Resolved resolve(Loader loader, PrototypeId id, Class<?> row, PickNodeDef def) {
        if (!def.path().isEmpty()) {
            String full = def.path();
            int slash = full.lastIndexOf('/');
            String dir = slash < 0 ? "" : full.substring(0, slash);
            String file = slash < 0 ? full : full.substring(slash + 1);
            String location = ResourceResolver.ROOT + "/" + full;
            ResourceResolver resolver = new ResourceResolver(loader, ResourceResolver.ROOT + (dir.isEmpty() ? "" : "/" + dir));
            return new Resolved(resolver, file, location, "path");
        }
        String file = fileName(row);
        if (def.commonDir()) {
            ResourceResolver resolver = ResourceResolver.root(loader, "common");
            return new Resolved(resolver, file, ResourceResolver.ROOT + "/common/" + file, "common");
        }
        ResourceResolver resolver = ResourceResolver.root(loader, id.path() + "/data");
        return new Resolved(resolver, file, ResourceResolver.ROOT + "/" + id.path() + "/data/" + file, "proto");
    }

    public static <T> List<T> loadYaml(Resolved resolved, Class<T> row) {
        return resolved.resolver().yaml(resolved.file(), row);
    }

    static String fileName(Class<?> row) {
        String snake = row.getSimpleName().replaceAll("([a-z])([A-Z])", "$1_$2");
        return snake.toLowerCase() + ".yaml";
    }
}
