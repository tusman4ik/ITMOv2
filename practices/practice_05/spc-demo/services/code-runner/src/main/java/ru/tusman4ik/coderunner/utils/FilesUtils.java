package ru.tusman4ik.coderunner.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;


@Slf4j
public class FilesUtils {
    private FilesUtils(){}

    public static void deleteDirRecursively(Path path) {
        try (var stream = Files.walk(path)) {
            boolean notAllDeleted = !stream
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .map(File::delete)
                    .reduce(true, Boolean::logicalAnd);

            if (notAllDeleted) {
                log.error("Cleaning dir {} failed!", path);
            } else {
                log.trace("Dir {} was cleaned successfully", path);
            }

        } catch (IOException e) {
            log.error("Failed to walk path during dir {} cleanup", path, e);
        }
    }
}
