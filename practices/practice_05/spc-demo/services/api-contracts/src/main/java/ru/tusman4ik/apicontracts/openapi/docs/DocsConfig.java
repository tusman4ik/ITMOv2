package ru.tusman4ik.apicontracts.openapi.docs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tusman4ik.apicontracts.openapi.PolymorphicCustomizer;
import ru.tusman4ik.apicontracts.openapi.SchemaDescriptionCustomizer;

import java.util.List;

@Configuration
public class DocsConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("api-contracts")
                .version("0.0.1-dev"));
    }

    @Bean
    public List<GroupedOpenApi> groups() {
        return List.of(
                GroupedOpenApi.builder()
                        .group("taskservice")
                        .pathsToMatch("/api/v1/tasks/**")
                        .addOpenApiCustomizer(polymorphicCustomizer())
                        .addOpenApiCustomizer(schemaDescriptionCustomizer())
                        .build(),
                GroupedOpenApi.builder()
                        .group("coderunner")
                        .pathsToMatch("/api/v1/submissions/**")
                        .addOpenApiCustomizer(polymorphicCustomizer())
                        .addOpenApiCustomizer(schemaDescriptionCustomizer())
                        .build());
    }

    private OpenApiCustomizer polymorphicCustomizer() {
        return new PolymorphicCustomizer();
    }

    private OpenApiCustomizer schemaDescriptionCustomizer() {
        return new SchemaDescriptionCustomizer();
    }

    @Bean
    public static ApiDocsRegistrar apiDocsRegistrar() {
        return new ApiDocsRegistrar();
    }
}
