package ru.tusman4ik.apicontracts.openapi;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import ru.tusman4ik.apicontracts.Root;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaDescriptionCustomizer implements OpenApiCustomizer {

    private static final String ROOT = Root.class.getPackageName();

    private final Map<String, Class<?>> classes = scanClasses();

    @Override
    @SuppressWarnings({"unchecked"})
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            applySchema(entry.getKey(), entry.getValue());
        }
    }

    private void applySchema(String name, Schema schema) {
        Class<?> type = classes.get(name);
        if (type == null) {
            return;
        }
        setDescription(schema, type);
        if (type.isRecord() && schema.getProperties() != null) {
            applyComponents(schema, type);
        }
    }

    private static void applyComponents(Schema schema, Class<?> type) {
        for (var component : type.getRecordComponents()) {
            Object property = schema.getProperties().get(component.getName());
            if (property instanceof Schema propertySchema
                    && propertySchema.get$ref() == null) {
                setDescription(propertySchema, propertyAnnotation(type, component.getName()));
            }
        }
    }

    private static void setDescription(Schema schema, AnnotatedElement element) {
        if (element == null) {
            return;
        }
        io.swagger.v3.oas.annotations.media.Schema annotation =
                element.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
        if (annotation != null && annotation.description() != null && !annotation.description().isEmpty()) {
            schema.setDescription(annotation.description());
        }
    }

    private static AnnotatedElement propertyAnnotation(Class<?> type, String name) {
        try {
            return type.getDeclaredField(name);
        } catch (NoSuchFieldException ignored) {
        }
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException ignored) {
        }
        for (var component : type.getRecordComponents()) {
            if (component.getName().equals(name)) {
                return component;
            }
        }
        return null;
    }

    private static Map<String, Class<?>> scanClasses() {
        Map<String, Class<?>> result = new HashMap<>();
        try (ScanResult scan = new ClassGraph()
                .acceptPackages(ROOT)
                .enableClassInfo()
                .scan()) {
            List<Class<?>> types = new ArrayList<>(scan.getAllClasses().loadClasses());
            types.sort(Comparator.comparing(Class::getName));
            for (Class<?> type : types) {
                result.putIfAbsent(type.getSimpleName(), type);
            }
        }
        return result;
    }
}
