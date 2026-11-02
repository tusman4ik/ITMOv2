package ru.tusman4ik.coderunner.models.mappers;

import ru.tusman4ik.coderunner.models.mappers.requests.submission.Context;

import java.util.List;

public interface IContext {
    List<String> getErrors();
    String getCurrentPos();
    Context goTo(String nextPartOfPath);
}
