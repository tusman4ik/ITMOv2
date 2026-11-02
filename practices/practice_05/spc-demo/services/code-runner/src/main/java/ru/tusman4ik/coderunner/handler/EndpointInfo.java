package ru.tusman4ik.coderunner.handler;

import io.javalin.openapi.HttpMethod;

public record EndpointInfo(HttpMethod method, String url) {
}
