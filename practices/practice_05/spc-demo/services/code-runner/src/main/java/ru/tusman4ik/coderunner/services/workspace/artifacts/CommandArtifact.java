package ru.tusman4ik.coderunner.services.workspace.artifacts;

import lombok.Builder;

import java.util.List;

@Builder
public record CommandArtifact(
        Meta meta, List<String> cmd
) implements Artifact {
}
