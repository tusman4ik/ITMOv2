plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.gmazzo.buildconfig") version "6.0.10"
}

group = "ru.tusman4ik"
version = "0.1.0-snapshot"
description = "task-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

buildConfig{
    useJavaOutput()
    packageName("ru.tusman4ik.taskservice")
    buildConfigField("VERSION", provider { "${project.version}" })
    buildConfigField("NAME", provider { project.name })
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {

    implementation(project(":services:api-contracts"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.retry:spring-retry:2.0.1")
    runtimeOnly("org.aspectj:aspectjweaver")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Apache poi
    implementation("org.apache.poi:poi:5.5.1")
    implementation("org.apache.poi:poi-ooxml:5.5.1")

    // JMustache
    implementation("com.samskivert:jmustache:1.16")

    // Morphy
    implementation("com.github.demidko:aot:2025.11.25")

    // JB annotations
    implementation("org.jetbrains:annotations:26.1.0")

    // Jackson YAML
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:3.2.2")

    // Jackson 2: api-contracts annotations are com.fasterxml-based, while Boot 4
    // defaults to tools.jackson. Outbound clients serialize with Jackson 2 explicitly.
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.0")

    // Logback
    implementation("ch.qos.logback:logback-classic:1.5.37")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")
    testImplementation("org.assertj:assertj-core:3.27.7")

    // Reflections
    implementation("io.github.classgraph:classgraph:4.8.192")
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "1g"
}