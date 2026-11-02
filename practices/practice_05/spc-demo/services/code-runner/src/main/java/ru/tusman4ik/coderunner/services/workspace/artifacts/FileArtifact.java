package ru.tusman4ik.coderunner.services.workspace.artifacts;

import java.nio.file.Path;

public record FileArtifact(
        Meta meta, Path path
) implements Artifact {

    public String strPath(){
        return path.toString();
    }
}
