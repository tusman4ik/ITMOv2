package ru.tusman4ik.coderunner.services.workspace.artifacts;


public interface Artifact {

    Meta meta();
    default String humanReadableName() {
        return getClass().getSimpleName();
    }
}
