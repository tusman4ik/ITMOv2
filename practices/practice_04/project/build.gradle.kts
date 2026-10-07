plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.tusman4ik"
version = "0.1.0-snapshot"
description = "spc-task-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web:4.1.1")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")

    // JMustache
    implementation("com.samskivert:jmustache:1.16")

    // Morphy
    implementation("com.github.demidko:aot:2025.11.25")

    // JB annotations
    implementation("org.jetbrains:annotations:26.1.0")

    // Jackson YAML
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:3.2.2")

    // Logback
    implementation("ch.qos.logback:logback-classic:1.5.37")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    // Reflections
    implementation("io.github.classgraph:classgraph:4.8.192")
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "1g"
}
