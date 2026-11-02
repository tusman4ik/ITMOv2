package ru.tusman4ik.taskservice.task.wire.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tusman4ik.taskservice.task.content.attachments.AttachmentCreator;
import ru.tusman4ik.taskservice.task.id.PrototypeId;
import ru.tusman4ik.taskservice.task.node.implementations.AttachmentNodeDef;
import ru.tusman4ik.taskservice.task.wire.PrototypeHandler;
import ru.tusman4ik.taskservice.task.wire.implementations.models.NodeSchema;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
@Component
public class AttachmentHandler implements PrototypeHandler {

    private final NodeScanner nodeScanner;

    public AttachmentHandler(NodeScanner nodeScanner) {
        this.nodeScanner = nodeScanner;
    }

    @Override
    public void handle(PrototypeId id) {

        nodeScanner.getNodesAnnotatedBy(id, AttachmentNodeDef.class)
                .stream()
                .peek(AttachmentHandler::throwIfNotAttachmentCreator)
                .forEach(f->log.debug("  - Node {} is correct attachment creator", f.name()));
    }

    private static void throwIfNotAttachmentCreator(NodeSchema schema) {
        Type nodeType = schema.node().getGenericType();

        if (!(nodeType instanceof ParameterizedType parameterizedType)) {
            throw invalidNode(schema, nodeType);
        }

        Type argument = parameterizedType.getActualTypeArguments()[0];

        Class<?> argumentClass = switch (argument) {
            case Class<?> c -> c;
            case ParameterizedType p when p.getRawType() instanceof Class<?> c -> c;
            default -> throw invalidNode(schema, argument);
        };

        if (!AttachmentCreator.class.isAssignableFrom(argumentClass)) {
            throw invalidNode(schema, argument);
        }
    }

    private static IllegalStateException invalidNode(NodeSchema schema, Type actualType) {
        return new IllegalStateException(
                "@AttachmentNodeDef field %s must use Node<T> where T implements AttachmentCreator; got %s"
                        .formatted(schema.node().toGenericString(), actualType.getTypeName())
        );
    }
}
