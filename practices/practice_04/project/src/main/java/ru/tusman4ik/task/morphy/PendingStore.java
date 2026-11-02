package ru.tusman4ik.task.morphy;

import com.github.demidko.aot.WordformMeaning;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PendingStore {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Path dir;

    public PendingStore(Path dir) {
        this.dir = dir;
    }

    public static Map<String, Object> dumpMeanings(String normalForm, List<WordformMeaning> meanings) {
        List<Map<String, Object>> dumped = new ArrayList<>();
        for (WordformMeaning meaning : meanings) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", meaning.getId());
            entry.put("partOfSpeech", String.valueOf(meaning.getPartOfSpeech()));
            List<Map<String, Object>> transformations = new ArrayList<>();
            for (WordformMeaning transformation : meaning.getTransformations()) {
                Map<String, Object> tr = new LinkedHashMap<>();
                tr.put("form", transformation.toString());
                tr.put("tags", transformation.getMorphology().stream().map(Object::toString).toList());
                transformations.add(tr);
            }
            entry.put("transformations", transformations);
            dumped.add(entry);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("normalForm", normalForm);
        result.put("meanings", dumped);
        return result;
    }

    public void save(String normalForm, String reason, List<String> tags, List<WordformMeaning> meanings) {
        try {
            Files.createDirectories(dir);
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("normalForm", normalForm);
            envelope.put("reason", reason);
            envelope.put("tags", tags);
            envelope.put("meanings", dumpMeanings(normalForm, meanings).get("meanings"));
            String fileName = URLEncoder.encode(normalForm, StandardCharsets.UTF_8) + ".json";
            Path target = dir.resolve(fileName);
            Path tmp = Files.createTempFile(dir, "pending-", ".json");
            Files.writeString(tmp, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(envelope), StandardCharsets.UTF_8);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            log.debug("Morphy pending saved: {} reason={}", target, reason);
        } catch (Exception e) {
            log.warn("Morphy pending save failed for '{}': {}", normalForm, e.toString());
        }
    }
}
