package ru.tusman4ik.taskservice.task.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PrototypeResources {

    private final Map<String, String> textCache = new ConcurrentHashMap<>();

    public Resource file(PrototypeId id, String name) {
        return new ClassPathResource("spc-res/" + id.path() + "/" + name);
    }

    public String text(PrototypeId id, String name) {
        return textCache.computeIfAbsent(cacheKey(id, name), _ -> read(id, name));
    }

    private String read(PrototypeId id, String name) {
        String location = "spc-res/" + id.path() + "/" + name;
        try (InputStream in = new ClassPathResource(location).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read resource " + location + " of " + id, e);
        }
    }

    private String cacheKey(PrototypeId id, String name) {
        return id.path() + "/" + name;
    }
}
