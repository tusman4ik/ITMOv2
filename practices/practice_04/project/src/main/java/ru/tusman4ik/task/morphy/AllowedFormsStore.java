package ru.tusman4ik.task.morphy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.tusman4ik.task.resources.Loader;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AllowedFormsStore {

    private final Loader loader;
    private final String allowedLocation;
    private final String requiredLocation;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public AllowedFormsStore(Loader loader,
                             @Value("${morphy.allowed:morphy/allowed-forms.json}") String allowedLocation,
                             @Value("${morphy.required:morphy/required-words.txt}") String requiredLocation) {
        this.loader = loader;
        this.allowedLocation = allowedLocation;
        this.requiredLocation = requiredLocation;
    }

    String allowedLocation() {
        return allowedLocation;
    }

    record AllowedEntry(Map<String, List<String>> meanings, String note) {}

    public Map<String, Map<Long, Set<String>>> loadAllowed() {
        String json = loader.text(allowedLocation);
        Map<String, AllowedEntry> raw = new LinkedHashMap<>();
        try {
            JsonNode root = MAPPER.readTree(json);
            if (!root.isObject()) {
                throw new IllegalStateException("root must be an object in " + allowedLocation);
            }
            root.properties().forEach(prop -> {
                if (prop.getKey().startsWith("_")) {
                    return;
                }
                try {
                    raw.put(prop.getKey(), MAPPER.convertValue(prop.getValue(), AllowedEntry.class));
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot parse entry '" + prop.getKey() + "' in " + allowedLocation, e);
                }
            });
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse " + allowedLocation, e);
        }
        Map<String, Map<Long, Set<String>>> result = new LinkedHashMap<>();
        for (Map.Entry<String, AllowedEntry> entry : raw.entrySet()) {
            AllowedEntry decoded = entry.getValue();
            Map<String, List<String>> meanings = decoded == null || decoded.meanings() == null
                    ? Map.of()
                    : decoded.meanings();
            Map<Long, Set<String>> parsed = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> meaning : meanings.entrySet()) {
                long id;
                try {
                    id = Long.parseLong(meaning.getKey());
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Bad meaning id '" + meaning.getKey() + "' for word '" + entry.getKey() + "' in " + allowedLocation);
                }
                parsed.put(id, meaning.getValue() == null ? Set.of() : Set.copyOf(meaning.getValue()));
            }
            result.put(entry.getKey(), parsed);
        }
        log.debug("Allowed forms loaded: {} words", result.size());
        return result;
    }

    public List<String> loadRequired() {
        String text = loader.text(requiredLocation);
        List<String> words = text.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toList());
        log.debug("Required words loaded: {}", words.size());
        return words;
    }
}
