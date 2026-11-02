package ru.tusman4ik.apicontracts.openapi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import ru.tusman4ik.apicontracts.taskservice.check.CheckResult;
import ru.tusman4ik.apicontracts.taskservice.check.Feedback;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;
import ru.tusman4ik.apicontracts.taskservice.form.InputForm;
import ru.tusman4ik.apicontracts.taskservice.task.content.Parameter;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PolymorphicInvariantGuardTest {

    private static final Set<Class<?>> EXPECTED_BASES = Set.of(
            InputSchema.class, InputForm.class, Feedback.class,
            CheckResult.class, Parameter.class
    );

    private static Set<Class<?>> ALL_BASES;

    @BeforeAll
    public static void init() {
        ALL_BASES = JacksonPolymorphism.findBases();
    }

    @Disabled
    @Test
    public void allExpectedBasesFound() {
        assertThat(EXPECTED_BASES)
                .allMatch(clazz -> ALL_BASES.contains(clazz));
    }

    @FieldSource("ALL_BASES")
    @ParameterizedTest
    public void allBasesAreSealed(Class<?> base) {
        assertTrue(base.isSealed());
    }

    @FieldSource("ALL_BASES")
    @ParameterizedTest
    public void allBasesContainsValidJsonTypeInfoAnnotation(Class<?> base) {
        JsonTypeInfo info = base.getAnnotation(JsonTypeInfo.class);
        assertNotNull(info);
        assertEquals(JsonTypeInfo.Id.NAME, info.use());
        assertEquals(JsonTypeInfo.As.PROPERTY, info.include());
        assertThat(info.property()).isNotBlank();
    }

    @FieldSource("ALL_BASES")
    @ParameterizedTest
    public void allBasesContainsValidJsonSubTypesAnnotation(Class<?> base) {
        JsonSubTypes subTypesAnnotation = base.getAnnotation(JsonSubTypes.class);
        assertNotNull(subTypesAnnotation);

        var subTypes = subTypesAnnotation.value();

        assertThat(subTypes).isNotEmpty();

        assertEquals(
                subTypes.length,
                Arrays.stream(subTypes)
                        .map(JsonSubTypes.Type::name)
                        .filter(t -> !t.isBlank())
                        .distinct()
                        .count()
        );

        assertThat(subTypes).allMatch(s -> base.isAssignableFrom(s.value()));
    }


}
