package ru.tusman4ik.apicontracts.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import ru.tusman4ik.apicontracts.taskservice.form.InputForm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class PolymorphicCustomizer implements OpenApiCustomizer {

    @Override
    @SuppressWarnings({"rawtypes"})
    public void customise(@NonNull OpenAPI openApi) {
        Map<String, Schema> schemas = schemasOf(openApi);
        for (Base base : PolymorphicEntityIndex.BASES) {
            applyBase(schemas, base);
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static Map<String, Schema> schemasOf(@NonNull OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            throw new IllegalStateException("OpenAPI components.schemas is missing, cannot apply polymorphism");
        }
        return openApi.getComponents().getSchemas();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void applyBase(Map<String, Schema> schemas, Base base) {
        Schema baseSchema = schemas.get(PolymorphicEntityIndex.schemaName(base.base()));
        if (baseSchema == null) {
            log.warn("base {} is not present in this spec, skipping", base.base().getName());
            return;
        }
        Map<String, String> mapping = new LinkedHashMap<>();
        List<Schema> oneOf = new ArrayList<>();
        for (Subtype subtype : base.subtypes()) {
            String ref = PolymorphicEntityIndex.schemaRef(subtype.type());
            mapping.put(subtype.wireName(), ref);
            oneOf.add(new Schema().$ref(ref));
            fixSubtype(schemas, base, subtype);
        }
        baseSchema.setDiscriminator(
                new Discriminator().propertyName(base.discriminatorProperty()).mapping(mapping));
        baseSchema.setOneOf(oneOf);
        baseSchema.setProperties(null);
        baseSchema.setRequired(null);
        baseSchema.setType(null);
        baseSchema.setTypes(null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void fixSubtype(Map<String, Schema> schemas, Base base, Subtype subtype) {
        Schema subtypeSchema = resolveSubtypeSchema(schemas, base, subtype);
        stripBaseRef(subtypeSchema, PolymorphicEntityIndex.schemaRef(base.base()));
        ensureObjectType(subtypeSchema);
        putEnumProperty(subtypeSchema, base.discriminatorProperty(),
                subtype.wireName(), "Polymorphic type discriminator");
//        if (base.base() == InputForm.class) {
//            pinLinkProperty(subtypeSchema, PolymorphicEntityIndex.relatedInputSchemaWireName((Class<? extends InputForm<?>>) subtype.type()));
//        }
        requireProperty(subtypeSchema, base.discriminatorProperty());
    }

    @SuppressWarnings({"rawtypes"})
    private static Schema resolveSubtypeSchema(Map<String, Schema> schemas, Base base, Subtype subtype) {
        Schema subtypeSchema = schemas.get(PolymorphicEntityIndex.schemaName(subtype.type()));
        if (subtypeSchema == null) {
            throw new IllegalStateException("Schema for subtype " + subtype.type().getName()
                    + " of base " + base.base().getName() + " is missing from the spec");
        }
        return subtypeSchema;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void stripBaseRef(Schema subtypeSchema, String baseRef) {
        if (subtypeSchema.getAllOf() == null) {
            return;
        }
        List<Schema> rest = new ArrayList<>();
        for (Object item : subtypeSchema.getAllOf()) {
            Schema part = (Schema) item;
            if (!baseRef.equals(part.get$ref())) {
                rest.add(part);
            }
        }
        subtypeSchema.setAllOf(rest.isEmpty() ? null : rest);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void ensureObjectType(Schema subtypeSchema) {
        if (subtypeSchema.getProperties() == null) {
            subtypeSchema.setProperties(new LinkedHashMap<>());
        }
        subtypeSchema.setType("object");
        subtypeSchema.setTypes(Set.of("object"));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void putEnumProperty(Schema subtypeSchema, String property, String value, String description) {
        subtypeSchema.getProperties().put(property,
                new StringSchema()._enum(List.of(value)).description(description));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void requireProperty(Schema subtypeSchema, String property) {
        List<String> required = subtypeSchema.getRequired() != null
                ? new ArrayList<>(subtypeSchema.getRequired())
                : new ArrayList<>();
        if (!required.contains(property)) {
            required.add(property);
        }
        subtypeSchema.setRequired(required);
    }

    @SuppressWarnings({"rawtypes"})
    private static void pinLinkProperty(Schema subtypeSchema, String wireName) {
        putEnumProperty(subtypeSchema, "schemaDiscriminator", wireName, "Related schema wire name");
        requireProperty(subtypeSchema, "schemaDiscriminator");
    }
}
