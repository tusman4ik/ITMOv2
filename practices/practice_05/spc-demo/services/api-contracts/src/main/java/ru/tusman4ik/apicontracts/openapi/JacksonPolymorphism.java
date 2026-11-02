package ru.tusman4ik.apicontracts.openapi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import ru.tusman4ik.apicontracts.Root;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;

import java.util.*;
import java.util.stream.Collectors;

public final class JacksonPolymorphism {

    private static final String[] BASE_PACKAGES = {
            Root.class.getPackageName()
    };

    private static volatile Set<Class<?>> basesCache;

    private JacksonPolymorphism() {
    }

    public static Set<Class<?>> findBases() {
        Set<Class<?>> cached = basesCache;
        if (cached == null) {
            synchronized (JacksonPolymorphism.class) {
                cached = basesCache;
                if (cached == null) {
                    cached = scanBases();
                    basesCache = cached;
                }
            }
        }
        return cached;
    }

    private static Set<Class<?>> scanBases() {
        try (ScanResult scan = new ClassGraph()
                .acceptPackages(BASE_PACKAGES)
                .enableAnnotationInfo()
                .scan()) {
            return scan.getClassesWithAnnotation(JsonTypeInfo.class).loadClasses().stream()
                    .collect(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparing(Class::getName))));
        }
    }


    public static String discriminatorProperty(Class<?> base) {
        JsonTypeInfo info = Objects.requireNonNull(
                base.getAnnotation(JsonTypeInfo.class), () -> "no @JsonTypeInfo on " + base);
        if (info.property() == null || info.property().isEmpty()) {
            throw new IllegalStateException("empty discriminator property on " + base);
        }
        return info.property();
    }


    public static LinkedHashMap<String, Class<?>> subtypes(Class<?> base) {
        JsonSubTypes subs = Objects.requireNonNull(
                base.getAnnotation(JsonSubTypes.class), () -> "no @JsonSubTypes on " + base);
        LinkedHashMap<String, Class<?>> result = new LinkedHashMap<>();
        for (JsonSubTypes.Type t : subs.value()) {
            if (t.name() == null || t.name().isEmpty()) {
                throw new IllegalStateException("blank subtype name on " + base);
            }
            result.put(t.name(), t.value());
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("empty @JsonSubTypes on " + base);
        }
        return result;
    }


    public static String schemaName(Class<?> type) {
        return type.getSimpleName();
    }


    public static String schemaRef(Class<?> type) {
        return "#/components/schemas/" + schemaName(type);
    }

    public static String relatedInputSchemaWireName(Class<?> form) {
        Class<?> schema = PolymorphicEntityIndex.relatedInputSchema(form);
        for (Class<?> base : findBases()) {
            for (Map.Entry<String, Class<?>> e : subtypes(base).entrySet()) {
                if (e.getValue().equals(schema)) {
                    return e.getKey();
                }
            }
        }
        throw new IllegalStateException("unregistered schema " + schema + " from " + form);
    }
}
