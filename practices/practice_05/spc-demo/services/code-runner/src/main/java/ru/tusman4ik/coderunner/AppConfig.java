package ru.tusman4ik.coderunner;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.coderunner.openapi.OpenApiProcessor;
import ru.tusman4ik.coderunner.BuildConfig;

/**
 * Namespace for system configurers
 *
 * @author tusman4ik
 * @since 0.2.0-alpha
 */
@Slf4j
public final class AppConfig {

    private AppConfig() {}

    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .build()
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

    /**
     * OpenAPI plugin configuration
     *
     * <p>
     * <ul>
     *     <li>Enables custom processing using {@link OpenApiProcessor}</li>
     *     <li>Declares author and contact information</li>
     *     <li>Declares version and name of the project</li>
     * </ul>
     *
     *
     */
    public static final OpenApiPlugin OPEN_API_PLUGIN = new OpenApiPlugin(
            openapi -> {
                openapi.withDefinitionProcessor(OpenApiProcessor::process);
                openapi.withDefinitionConfiguration((_, builder) -> {
                            builder.openApiVersion("3.1.0");
                            builder.info(info -> {
                                info.title(BuildConfig.NAME);
                                info.version(BuildConfig.VERSION);
                                info.withContact(contact ->
                                {
                                    contact.name("tusman4ik");
                                    contact.email("tusman4ik@gmail.com");
                                });
                            });
                        }
                );
            }
    );

    /**
     * Swagger UI plugin configuration
     *
     * <p>
     * Serves OpenAPI spec at {@code /openapi} and Swagger UI at {@code /swagger}
     *
     *
     */
    public static final SwaggerPlugin SWAGGER_PLUGIN = new SwaggerPlugin(swaggerConfig -> {
        swaggerConfig.documentationPath = "/openapi";
        swaggerConfig.uiPath = "/swagger";
    });
}
