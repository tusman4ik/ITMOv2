package ru.tusman4ik.taskservice.task.generator;

import ru.tusman4ik.apicontracts.taskservice.task.content.Parameter;
import ru.tusman4ik.taskservice.task.node.Node;

import java.util.List;

public interface ParameterProvider {
    List<Node<? extends Parameter>> rawParameters();
}
