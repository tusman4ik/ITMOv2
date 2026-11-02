package ru.tusman4ik.coderunner.services.workspace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;
import ru.tusman4ik.coderunner.services.workspace.artifacts.Artifact;
import ru.tusman4ik.coderunner.services.workspace.artifacts.FileArtifact;
import ru.tusman4ik.coderunner.services.workspace.artifacts.Meta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static ru.tusman4ik.coderunner.utils.FilesUtils.deleteDirRecursively;

@Slf4j
@RequiredArgsConstructor
public class Workspace implements AutoCloseable {

    public static final String STORAGE_FOLDER_NAME = "storage";
    public static final Set<PosixFilePermission> DEFAULT_FILE_PERMISSIONS =
            PosixFilePermissions.fromString("rw-rw-rw-");

    public static final Set<PosixFilePermission> DEFAULT_DIR_PERMISSIONS =
            PosixFilePermissions.fromString("rwxrwxrwx");

    @Getter
    private final Path path;
    private final List<Artifact> artifacts;
    private final Path storagePath;

    public Workspace() throws IOException {
        path = Files.createTempDirectory(Path.of("/dev/shm"), "submission-");
        Files.setPosixFilePermissions(path, DEFAULT_DIR_PERMISSIONS);
        artifacts = new ArrayList<>();
        storagePath = path.resolve(STORAGE_FOLDER_NAME);
        createDefaultFolderStructure();
        log.trace("Workspace {} created successfully", path);
    }

    public Workspace(Workspace other) throws IOException {
        path = Files.createTempDirectory(Path.of("/dev/shm"), "submission-");
        Files.setPosixFilePermissions(path, DEFAULT_DIR_PERMISSIONS);
        artifacts = new ArrayList<>(other.artifacts);
        storagePath = path.resolve(STORAGE_FOLDER_NAME);
        createDefaultFolderStructure();
        other.artifacts.stream()
                .filter(t -> t instanceof FileArtifact)
                .map(t -> (FileArtifact) t)
                .forEach(this::createLink);

        log.trace("Workspace {} inherited from {} successfully", path, other.getPath());
    }

    private void createLink(FileArtifact fileArtifact) {
        fileArtifact.meta().stages().forEach(stage -> {
            String folder = stage.name().toLowerCase(Locale.ROOT);
            Path link = path.resolve(folder).resolve(fileArtifact.path().getFileName());
            Path existing = fileArtifact.path();

            try {
                Files.createLink(link, existing);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public FileArtifact createFileArtifact(String name, Meta meta) throws IOException {
        return createFileArtifact(name, meta, DEFAULT_FILE_PERMISSIONS);
    }

    public FileArtifact createFileArtifact(String name, Meta meta, Set<PosixFilePermission> permissions) throws IOException {
        Path targetPath = storagePath.resolve(name);
        if (Files.exists(targetPath)) {
            log.error("Attempt create new artifact with existing path: {}", targetPath);
            throw new IllegalArgumentException("Attempt create new artifact with existing path");
        }
        Files.createFile(targetPath);
        FileArtifact artifact = new FileArtifact(meta, targetPath);
        Files.setPosixFilePermissions(targetPath, permissions);
        register(artifact);
        createLink(artifact);
        return artifact;
    }

    public Artifact registerFileArtifact(String name, String payload, Meta meta) throws IOException {
        return registerFileArtifact(name, payload, meta, DEFAULT_FILE_PERMISSIONS);
    }

    public Artifact registerFileArtifact(String name, String payload, Meta meta, Set<PosixFilePermission> permissions) throws IOException {
        FileArtifact artifact = createFileArtifact(name, meta, permissions);
        Path targetPath = artifact.path();
        Files.writeString(targetPath, payload);
        return artifact;
    }

    public void register(Artifact artifact) {
        log.trace("Artifact {} was registered successfully (mark: {}, stages: {})",
                artifact.humanReadableName(), artifact.meta().mark(), artifact.meta().stages());
        artifacts.add(artifact);
    }

    @Override
    public void close() {
        deleteDirRecursively(path);
    }


    private void createDefaultFolderStructure() {
        createFolder(STORAGE_FOLDER_NAME);
        Arrays.stream(Stage.values())
                .map(t -> t.name().toLowerCase(Locale.ROOT))
                .forEach(this::createFolder);
    }

    private void createFolder(String t) {
        try {
            Path targetPath = path.resolve(t);
            Files.createDirectories(targetPath);
            Files.setPosixFilePermissions(targetPath, DEFAULT_DIR_PERMISSIONS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked") //cause there is a filter
    public <T> List<T> findAll(Mark mark, Class<T> clazz) {
        return artifacts.stream()
                .filter(artifact -> artifact.meta().mark() == mark)
                .filter(artifact -> artifact.getClass().equals(clazz))
                .map(artifact -> (T) artifact)
                .toList();
    }

    public <T> T findOne(Mark mark, Class<T> clazz) {
        List<T> art = findAll(mark, clazz);
        if (art.size() != 1) {
            log.error("Expected 1 artifact with mark = {}, got = {}", mark, art.size());
            throw new IllegalArgumentException("???");
        }
        return art.getFirst();
    }

    public Path getStageDir(Stage stage) {
        return path.resolve(stage.name().toLowerCase());
    }
}
