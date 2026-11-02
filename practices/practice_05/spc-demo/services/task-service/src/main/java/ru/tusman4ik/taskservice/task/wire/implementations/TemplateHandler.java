package ru.tusman4ik.taskservice.task.wire.implementations;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tusman4ik.taskservice.task.generator.FrozenContext;
import ru.tusman4ik.taskservice.task.id.PrototypeId;
import ru.tusman4ik.taskservice.task.resources.PrototypeResources;
import ru.tusman4ik.taskservice.task.wire.PrototypeHandler;
import ru.tusman4ik.taskservice.task.wire.implementations.models.NodeFingerprint;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

@Slf4j
@Component
public class TemplateHandler implements PrototypeHandler {

    private static final String STATEMENT_FILE = "statement.mustache";

    private final FingerprintCollector fingerprintCollector;
    private final PrototypeResources prototypeResources;
    private final Map<PrototypeId, Template> templates = new HashMap<>();

    public TemplateHandler(FingerprintCollector fingerprintCollector, PrototypeResources prototypeResources) {
        this.fingerprintCollector = fingerprintCollector;
        this.prototypeResources = prototypeResources;
    }

    @Override
    public void handle(PrototypeId id) {
        String source = prototypeResources.text(id, STATEMENT_FILE);
        templates.put(id, Mustache.compiler().compile(source));
        log.debug("{}: statement template compiled", id);
    }

    public String render(PrototypeId id, FrozenContext frozen) {
        Template template = templates.get(id);
        if (template == null) {
            throw new IllegalStateException("statement template not compiled for " + id);
        }

        Map<String, Object> values = new LinkedHashMap<>();
        for (NodeFingerprint fingerprint : fingerprintCollector.getNodes(id)) {
            frozen.getOpt(fingerprint.node())
                    .ifPresent(value -> values.put(fingerprint.shema().name(), value));
        }
        return template.execute(values);
    }
}
