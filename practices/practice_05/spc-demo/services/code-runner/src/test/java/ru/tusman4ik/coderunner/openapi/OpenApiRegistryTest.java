package ru.tusman4ik.coderunner.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.tusman4ik.coderunner.AppConfig;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
class OpenApiRegistryTest {

    private static final ObjectMapper MAPPER = AppConfig.MAPPER;

    @ParameterizedTest
    @DisplayName("All examples should survive serialization roundtrip")
    @MethodSource("provideAllExamples")
    <T> void shouldSurviveSerializationRoundtrip(Class<T> clazz, T original) throws JsonProcessingException {
        // given
        String json = MAPPER.writeValueAsString(original);
        log.debug("class = {}", clazz.getSimpleName());
        log.debug("json = {}", json);
        log.debug("");
        // when (always?)
        T deserialized = MAPPER.readValue(json, clazz);

        // then
        assertThat(deserialized)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    static Stream<Arguments> provideAllExamples() {
        return OpenApiRegistry.getExamples()
                .entrySet()
                .stream()
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

}