package ru.tusman4ik.coderunner.openapi;

import lombok.Builder;

@Builder
public record EndpointExample(String summary, Object value) {
}
