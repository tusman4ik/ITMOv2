package ru.tusman4ik.coderunner.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.tusman4ik.coderunner.AppConfig;
import ru.tusman4ik.coderunner.handler.EndpointInfo;

import java.util.*;

import static ru.tusman4ik.coderunner.openapi.OpenApiRegistry.getEndpointExamples;
import static ru.tusman4ik.coderunner.openapi.OpenApiRegistry.getExamples;

@Slf4j
public final class OpenApiProcessor {

    private static final Set<String> IGNORED_PROPERTIES = Set.of(
            "or", "resolveDefaults", "resolve_defaults", "toBuilder", "to_builder"
    );

    private OpenApiProcessor() {}

    public static String process(@NonNull ObjectNode spec) {
        log.info("Default spec\n{}", spec.toPrettyString());

        ObjectNode schemas = extractSchemasNode(spec);

        injectCustomExamples(schemas);
        convertToSnakeCase(schemas);
        cleanupSchemas(schemas);
        injectCustomEndpointExamples(spec);

        return spec.toPrettyString();
    }

    private static ObjectNode extractSchemasNode(ObjectNode spec) {
        return (ObjectNode) spec.path("components").path("schemas");
    }

    private static void cleanupSchemas(ObjectNode schemas) {
        List<String> toRemove = new ArrayList<>();

        schemas.fieldNames().forEachRemaining(name -> {
            if (name != null && name.endsWith("Builder")) {
                toRemove.add(name);
            }
        });

        for (String name : toRemove) {
            log.info("Removing generated Builder schema: {}", name);
            schemas.remove(name);
        }
    }

    private static void convertToSnakeCase(ObjectNode schemas) {
        schemas.properties().forEach(entry -> {
            ObjectNode schema = (ObjectNode) entry.getValue();
            convertSchemaProperties(schema);
            convertSchemaRequiredFields(schema);
        });
    }

    private static void convertSchemaProperties(ObjectNode schema) {
        if (!schema.has("properties")) {
            return;
        }

        ObjectNode properties = (ObjectNode) schema.path("properties");
        ObjectNode newProperties = AppConfig.MAPPER.createObjectNode();

        properties.properties().forEach(prop -> {
            String key = prop.getKey();
            if (IGNORED_PROPERTIES.contains(key)) {
                log.info("Skipping helper property: {}", key);
            } else {
                newProperties.set(toSnakeCase(key), prop.getValue());
            }
        });

        schema.set("properties", newProperties);
    }

    private static void convertSchemaRequiredFields(ObjectNode schema) {
        if (schema.has("required") && schema.get("required").isArray()) {
            ArrayNode requiredArray = (ArrayNode) schema.get("required");
            ArrayNode newRequired = AppConfig.MAPPER.createArrayNode();

            requiredArray.forEach(req -> newRequired.add(toSnakeCase(req.asText())));
            schema.set("required", newRequired);
        }
    }

    private static String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private static void injectCustomEndpointExamples(@NonNull ObjectNode spec) {
        getEndpointExamples().forEach((info, value) -> injectEndpointExample(spec, info, value));
    }

    private static void injectEndpointExample(@NotNull ObjectNode spec, EndpointInfo info, Map<String, EndpointExample> value) {
        JsonNode targetEndpoint = spec.path("paths")
                .path(info.url())
                .path(info.method().name().toLowerCase());

        if (targetEndpoint.isMissingNode()) {
            log.error("Attempt to add examples to missing node! {} {}", info.method(), info.url());
            return;
        }

        ObjectNode targetNode = (ObjectNode) targetEndpoint.path("requestBody")
                .path("content")
                .path("application/json");

        if (targetNode.isMissingNode()) {
            log.error("Internal error: node path/{}/{} found, but not his expected child node requestBody/content/application/json",
                    info.method(), info.url());
            return;
        }

        targetNode.set("examples", AppConfig.MAPPER.valueToTree(value));
    }

    private static void injectCustomExamples(ObjectNode schemas) {
        getExamples().entrySet().stream()
                .map(OpenApiProcessor::serializeObj)
                .peek(OpenApiProcessor::logRegistryEntry)
                .map(OpenApiProcessor::extractSimpleName)
                .forEach(entry -> injectExampleToSchema(entry, schemas));
    }

    private static void injectExampleToSchema(Map.Entry<String, JsonNode> entry, ObjectNode schemas) {
        JsonNode schemaNode = schemas.path(entry.getKey());

        if (schemaNode.isMissingNode()) {
            log.warn("Schema was registered, but not found in the openApi spec for '{}'!", entry.getKey());
        } else {
            ((ObjectNode) schemaNode).set("example", entry.getValue());
        }
    }

    @NotNull
    private static AbstractMap.SimpleEntry<Class<?>, JsonNode> serializeObj(Map.Entry<Class<?>, Object> entry) {
        return new AbstractMap.SimpleEntry<>(entry.getKey(), AppConfig.MAPPER.valueToTree(entry.getValue()));
    }

    private static void logRegistryEntry(Map.Entry<Class<?>, JsonNode> entry) {
        log.debug("Found example for: '{}'\n{}", entry.getKey().getSimpleName(), entry.getValue().toPrettyString());
    }

    private static Map.Entry<String, JsonNode> extractSimpleName(Map.Entry<Class<?>, JsonNode> entry) {
        return new AbstractMap.SimpleEntry<>(entry.getKey().getSimpleName(), entry.getValue());
    }
}