package ru.tusman4ik.task.resources;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class FileResourceLoader implements Loader {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private final ResourceLoader resources;
    private final Map<String, String> texts = new ConcurrentHashMap<>();
    private final Map<String, byte[]> blobs = new ConcurrentHashMap<>();
    private final Map<List<Object>, List<?>> yamls = new ConcurrentHashMap<>();

    public FileResourceLoader(ResourceLoader resources) {
        this.resources = resources;
    }

    @Override
    public String text(String location) {
        return texts.computeIfAbsent(location, this::readText);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> yaml(String location, Class<T> type) {
        return (List<T>) yamls.computeIfAbsent(List.of(location, type), ignored -> readYaml(location, type));
    }

    @Override
    public byte[] blob(String location) {
        return blobs.computeIfAbsent(location, this::readBlob);
    }

    private Resource resource(String location) {
        return resources.getResource("classpath:" + location);
    }

    private String readText(String location) {
        try (InputStream input = resource(location).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Resource not found: " + location, e);
        }
    }

    private byte[] readBlob(String location) {
        try (InputStream input = resource(location).getInputStream()) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Resource not found: " + location, e);
        }
    }

    private <T> List<T> readYaml(String location, Class<T> type) {
        JavaType listType = YAML.getTypeFactory().constructCollectionType(List.class, type);
        try (InputStream input = resource(location).getInputStream()) {
            List<T> values = YAML.readValue(input, listType);
            return List.copyOf(values);
        } catch (IOException e) {
            throw new IllegalStateException("Resource not found: " + location, e);
        }
    }
}
