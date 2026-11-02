plugins {
    id("java-library")
}

group = "ru.tusman4ik"
version = ""

java {
    toolchain { languageVersion = JavaLanguageVersion.of(26) }
}

repositories {
    mavenCentral()
}

dependencies {
    // Jackson for JSON serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.0")

    // OpenAPI annotations (jakarta flavor, aligned with springdoc in task-service)
    api("io.swagger.core.v3:swagger-annotations-jakarta:2.2.55")

    // Validation
    api("jakarta.validation:jakarta.validation-api:3.1.0")

    // Classpath scanning for the polymorphic index
    implementation("io.github.classgraph:classgraph:4.8.192")

    compileOnly("org.springdoc:springdoc-openapi-starter-common:3.1.1")
    testImplementation("org.springdoc:springdoc-openapi-starter-common:3.1.1")

    // Dev-only docs application: compileOnly + docsRuntime so spring/springdoc never leak into consumers
    implementation("org.springframework.boot:spring-boot-starter-web:4.1.1")
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-api:3.1.1")

    // Logging (for @Slf4j)
    implementation("org.slf4j:slf4j-api:2.0.13")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

val docsRun = configurations.create("docsRun") {
    isCanBeConsumed = false
    isCanBeResolved = true
    extendsFrom(configurations.getByName("implementation"), configurations.getByName("runtimeOnly"))
}

dependencies {
    add("docsRun", "org.springframework.boot:spring-boot-starter-web:4.1.1")
    add("docsRun", "org.springdoc:springdoc-openapi-starter-common:3.1.1")
    add("docsRun", "org.springdoc:springdoc-openapi-starter-webmvc-api:3.1.1")
}

val docsCp = "${sourceSets.main.get().output.asPath}:${configurations.getByName("docsRun").asPath}"

val docsAppMainClass = "ru.tusman4ik.apicontracts.openapi.docs.DocsApp"

tasks.register<Exec>("generateApiDocs") {
    dependsOn(tasks.named("compileJava"))
    group = "documentation"
    description = "Exports /v3/api-docs JSON for both groups from the dev DocsApp"
    commandLine(
        "bash", "-c",
        """
        DOCS_CP='$docsCp'
        echo "${'$'}DOCS_CP" > build/docs-cp.txt
        mkdir -p openapi ts
        java -cp "${'$'}DOCS_CP" $docsAppMainClass --server.port=8099 > build/docs-app.log 2>&1 &
        PID=${'$'}!
        trap 'kill ${'$'}PID 2>/dev/null || kill -9 ${'$'}PID 2>/dev/null || true' EXIT
        for i in ${'$'}(seq 1 60); do
            curl -sf http://localhost:8099/v3/api-docs/taskservice -o /dev/null && break
            sleep 1
        done
        if [ "${'$'}DEBUG_DOCS" = "1" ]; then sleep 120; fi
        curl -sf http://localhost:8099/v3/api-docs/taskservice -o openapi/taskservice.json || exit 1
        curl -sf http://localhost:8099/v3/api-docs/coderunner -o openapi/coderunner.json || exit 1
        trap - EXIT
        kill ${'$'}PID 2>/dev/null || kill -9 ${'$'}PID 2>/dev/null || true
        """.trimIndent()
    )
}

tasks.register<Exec>("generateTsApi") {
    dependsOn(tasks.named("generateApiDocs"))
    group = "documentation"
    description = "Generates TS client types from the exported OpenAPI JSON"
    commandLine(
        "bash", "-c",
        """
        if command -v openapi-typescript >/dev/null 2>&1; then
            CLI=openapi-typescript
        else
            CLI='npx --yes openapi-typescript'
        fi
        ${'$'}CLI openapi/taskservice.json -o ts/taskservice.d.ts
        ${'$'}CLI openapi/coderunner.json -o ts/coderunner.d.ts
        """.trimIndent()
    )
}