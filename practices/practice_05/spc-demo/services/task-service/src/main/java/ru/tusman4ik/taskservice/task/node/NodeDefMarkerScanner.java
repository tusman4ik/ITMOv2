package ru.tusman4ik.taskservice.task.node;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tusman4ik.taskservice.vo.BasePackageName;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NodeDefMarkerScanner {
    private final BasePackageName ROOT;
    @Getter
    private final Set<Class<? extends Annotation>> nodeAnnotations = new HashSet<>();

    public NodeDefMarkerScanner(BasePackageName root) {ROOT = root;}

    @PostConstruct
    public void init() {
        try (ScanResult scan = new ClassGraph()
                .enableAnnotationInfo()
                .acceptPackages(ROOT.name())
                .scan()) {

            nodeAnnotations.addAll(
                    scan.getClassesWithAnnotation(NodeDefMarker.class.getName())
                            .stream()
                            .filter(ClassInfo::isAnnotation)
                            .map(ClassInfo::loadClass)
                            .map(type -> type.<Annotation>asSubclass(Annotation.class))
                            .collect(Collectors.toSet())
            );

            logPostConstructResult();
        }
    }

    private void logPostConstructResult() {
        log.debug("Found {} node annotations:", nodeAnnotations.size());
        nodeAnnotations.stream()
                .map(Class::getSimpleName)
                .sorted()
                .forEach(name -> log.debug("  - @{}", name));
    }

    public boolean contains(Class<? extends Annotation> clazz) {
        return nodeAnnotations.contains(clazz);
    }
}
