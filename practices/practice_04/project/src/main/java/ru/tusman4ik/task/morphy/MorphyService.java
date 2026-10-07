package ru.tusman4ik.task.morphy;

import com.github.demidko.aot.WordformMeaning;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MorphyService {

    private final Map<String, Map<Long, Set<String>>> allowed;
    private final PendingStore pending;
    private final String allowedLocation;
    private final Map<WordForm, String> cache = new ConcurrentHashMap<>();

    public MorphyService(Map<String, Map<Long, Set<String>>> allowed, PendingStore pending, String allowedLocation) {
        Map<String, Map<Long, Set<String>>> deep = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Long, Set<String>>> entry : allowed.entrySet()) {
            Map<Long, Set<String>> inner = new LinkedHashMap<>();
            for (Map.Entry<Long, Set<String>> meaning : entry.getValue().entrySet()) {
                inner.put(meaning.getKey(), Set.copyOf(meaning.getValue()));
            }
            deep.put(entry.getKey(), inner);
        }
        this.allowed = Map.copyOf(deep);
        this.pending = Objects.requireNonNull(pending, "pending store must be not null");
        this.allowedLocation = Objects.requireNonNull(allowedLocation, "allowed location must be not null");
    }

    public String resolve(WordForm request) {
        Objects.requireNonNull(request, "word form request must be not null");
        return cache.computeIfAbsent(request, this::doResolve);
    }

    private String doResolve(WordForm request) {
        String nf = request.normalForm();
        List<String> tagNames = request.tags().stream().map(Object::toString).toList();
        Map<Long, Set<String>> allowedMeanings = allowed.get(nf);
        List<WordformMeaning> meanings = WordformMeaning.lookupForMeanings(nf);
        if (allowedMeanings == null || allowedMeanings.isEmpty()
                || allowedMeanings.values().stream().allMatch(Set::isEmpty)) {
            pending.save(nf, "missing", tagNames, meanings);
            throw new MissingWordException(nf, allowedLocation);
        }
        log.trace("Morphy resolve enter: nf={} tags={} allowedMeanings={}", nf, request.tags(), allowedMeanings.keySet());
        log.trace("Morphy lookup: nf={} meanings={}", nf, meanings.size());

        List<Long> matchedMeaningIds = new ArrayList<>();
        Set<String> result = new LinkedHashSet<>();
        for (WordformMeaning meaning : meanings) {
            Set<String> allowedForms = allowedMeanings.get(meaning.getId());
            if (allowedForms == null || allowedForms.isEmpty()) {
                continue;
            }
            for (WordformMeaning transformation : meaning.getTransformations()) {
                if (!new HashSet<>(transformation.getMorphology()).containsAll(request.tags())) {
                    continue;
                }
                String form = transformation.toString();
                if (!allowedForms.contains(form)) {
                    log.trace("Morphy skip disallowed: nf={} meaning={} form={}", nf, meaning.getId(), form);
                    continue;
                }
                result.add(form);
                if (!matchedMeaningIds.contains(meaning.getId())) {
                    matchedMeaningIds.add(meaning.getId());
                }
                log.trace("Morphy match: nf={} meaning={} form={} tags={}",
                        nf, meaning.getId(), form, transformation.getMorphology());
            }
        }

        if (result.isEmpty()) {
            log.debug("Morphy no match: nf={} tags={}", nf, request.tags());
            pending.save(nf, "nosuchform", tagNames, meanings);
            throw new NoSuchFormException(nf, tagNames);
        }
        if (result.size() > 1) {
            log.debug("Morphy ambiguous: nf={} tags={} candidates={} meanings={}",
                    nf, request.tags(), result, matchedMeaningIds);
            pending.save(nf, "ambiguous", tagNames, meanings);
            throw new AmbiguousMeaningException(nf, List.copyOf(matchedMeaningIds), Set.copyOf(result));
        }
        String form = result.iterator().next();
        log.debug("Morphy resolved: nf={} tags={} -> {}", nf, request.tags(), form);
        return form;
    }
}
