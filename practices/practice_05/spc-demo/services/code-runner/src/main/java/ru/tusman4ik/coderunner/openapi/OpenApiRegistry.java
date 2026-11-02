package ru.tusman4ik.coderunner.openapi;

import io.javalin.openapi.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.coderunner.handler.EndpointInfo;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class OpenApiRegistry {

    private OpenApiRegistry() {}

    private static final Map<Class<?>, Object> EXAMPLES = new HashMap<>();
    private static final Map<EndpointInfo, Map<String, EndpointExample>> ENDPOINT_EXAMPLES = new HashMap<>();


    static {
        registerExample(OpenApiExampleFactory.submissionRequest());
        registerEndpointExample(new EndpointInfo(HttpMethod.POST, "/api/v1/submissions"),
                Map.of(
                        "fst ex",
                        new EndpointExample("", OpenApiExampleFactory.submissionRequest())

                )
        );
    }

    public static <T> void registerExample(T t) {
        EXAMPLES.put(t.getClass(), t);
    }

    public static Map<Class<?>, Object> getExamples() {
        return EXAMPLES;
    }

    public static void registerEndpointExample(EndpointInfo info, Map<String, EndpointExample> t) {
        ENDPOINT_EXAMPLES.put(info, t);
    }

    public static Map<EndpointInfo, Map<String, EndpointExample>> getEndpointExamples() {
        return ENDPOINT_EXAMPLES;
    }

}
