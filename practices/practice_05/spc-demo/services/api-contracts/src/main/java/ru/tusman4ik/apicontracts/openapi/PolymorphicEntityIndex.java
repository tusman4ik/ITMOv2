package ru.tusman4ik.apicontracts.openapi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.Root;
import ru.tusman4ik.apicontracts.taskservice.form.InputForm;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class PolymorphicEntityIndex {

    private static final String ROOT = Root.class.getPackageName();

    public static final Set<Base> BASES = scanBases();

    public static String schemaName(Class<?> type) {
        return type.getSimpleName();
    }

    public static String schemaRef(Class<?> type) {
        return "#/components/schemas/" + schemaName(type);
    }

    public static Class<? extends InputSchema> relatedInputSchema(
            @NonNull Class<?> form
    ) {
        Type type = form.getGenericInterfaces()[0];

        if (!(type instanceof ParameterizedType parameterizedType)) {
            throw new IllegalStateException(
                    "InputForm must declare its schema type: " + form.getName()
            );
        }

        Type argument = parameterizedType.getActualTypeArguments()[0];

        if (argument instanceof Class<?> c
                && InputSchema.class.isAssignableFrom(c)) {
            return c.asSubclass(InputSchema.class);
        }

        throw new IllegalStateException(
                "Invalid input schema type on " + form.getName() + ": " + argument
        );
    }

    public static String relatedInputSchemaWireName(@NonNull Class<?> form) {
        if (!InputForm.class.isAssignableFrom(form)) {
            throw new IllegalStateException(
                    "@RelatedInputSchema is only supported on InputForm children, but got " + form.getName());
        }

        Class<?> schemaType = relatedInputSchema(form);
        Base schemaBase = BASES.stream()
                .filter(base -> base.base().equals(InputSchema.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "InputSchema base is missing from the polymorphic index, requested by " + form.getName()
                ));

        return schemaBase.subtypes()
                .stream()
                .filter(t -> schemaType.equals(t.type()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Schema " + schemaType.getName() + " has no wire name in @JsonSubTypes of InputSchema"
                                + ", requested by " + form.getName()
                ))
                .wireName();
    }

    private static Set<Base> scanBases() {
        try (ScanResult scan = new ClassGraph()
                .acceptPackages(ROOT)
                .enableAnnotationInfo()
                .scan()) {
            Set<Base> bases = new HashSet<>();
            for (Class<?> base : scan.getClassesWithAnnotation(JsonTypeInfo.class).loadClasses()) {
                bases.add(extractBase(base));
            }
            logIndex(bases);
            return Collections.unmodifiableSet(bases);
        }
    }

    private static Base extractBase(Class<?> base) {
        JsonTypeInfo info = base.getAnnotation(JsonTypeInfo.class);
        JsonSubTypes subTypes = base.getAnnotation(JsonSubTypes.class);

        if (subTypes == null) {
            throw new IllegalStateException("@JsonTypeInfo without @JsonSubTypes on " + base.getName());
        }
        List<Subtype> subtypes = Arrays.stream(subTypes.value())
                .map(t -> new Subtype(t.name(), t.value()))
                .toList();

        log.debug("For '{}' found {} subtypes: {}",
                base.getSimpleName(), subtypes.size(), subtypes.stream().map(Subtype::wireName).sorted().toList());

        return new Base(
                base,
                info.property(),
                subtypes
        );
    }

    private static void logIndex(Set<Base> bases) {
        List<String> names = bases.stream()
                .map(b -> b.base().getSimpleName())
                .sorted()
                .toList();
        log.info("Indexed {} polymorphic bases: {}", bases.size(), names);
    }
}
