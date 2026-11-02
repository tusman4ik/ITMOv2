plugins {
    id("java")
    application
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.gmazzo.buildconfig") version "6.0.10"
}

group = "ru.tusman4ik"
version = "0.2.0-alpha"

var appMainClass = "ru.tusman4ik.coderunner.App"

repositories {
    mavenCentral()
}

application {
    mainClass = appMainClass
}

buildConfig{
    useJavaOutput()
    packageName("ru.tusman4ik.coderunner")
    buildConfigField("VERSION", provider { "${project.version}" })
    buildConfigField("NAME", provider { project.name })
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = appMainClass
    }
}


tasks.register<Exec>("dockerBuild") {
    group = "build"
    description = "Builds the docker container (repo-root context, see services/code-runner/Dockerfile)"

    // NOTE: the Dockerfile is multi-stage and expects the repository root as context.
    commandLine(
        "docker", "build", "-t",
        "tusman4ik/" + project.name + ":" + project.version, "-t",
        "tusman4ik/" + project.name + ":latest",
        "-f", project.projectDir.resolve("Dockerfile").absolutePath,
        project.projectDir.parentFile.parentFile.absolutePath
    )
    standardOutput = System.out
    errorOutput = System.err
}

dependencies {
    val openapi = "7.2.2"

    // Internal
    implementation(project(":services:api-contracts"))

    // JB
    implementation("org.jetbrains:annotations:26.1.0")

    // Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.46")

    compileOnly("org.projectlombok:lombok:1.18.46")
    testCompileOnly("org.projectlombok:lombok:1.18.46")

    // Logback
    implementation("ch.qos.logback:logback-classic:1.5.37")
    testImplementation("ch.qos.logback:logback-classic:1.5.37")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    // Reflections
    implementation("org.reflections:reflections:0.10.2")

    // OpenApi + swagger
    annotationProcessor("io.javalin.community.openapi:openapi-annotation-processor:$openapi")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:$openapi")
    implementation("io.javalin.community.openapi:javalin-swagger-plugin:$openapi")
    implementation("io.javalin.community.openapi:javalin-redoc-plugin:$openapi")

    // Javalin
    implementation("io.javalin:javalin:7.2.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.6.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencies {
    constraints {
        implementation("com.fasterxml.jackson.core:jackson-core:2.22.0") {
            because("fixes vulnerability WS-2026-0003")
        }
        implementation("com.fasterxml.jackson.core:jackson-databind:2.22.0") {
            because("fixes vulnerability WS-2026-0003")
        }
        implementation("tools.jackson.core:jackson-core:3.2.0") {
            because("fixes CVE-2026-29062 StackOverflow DoS")
        }
        implementation("tools.jackson.core:jackson-databind:3.2.0") {
            because("fixes CVE-2026-29062 StackOverflow DoS")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}