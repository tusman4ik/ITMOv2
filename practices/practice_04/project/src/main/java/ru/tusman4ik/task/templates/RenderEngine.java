package ru.tusman4ik.task.templates;

import java.util.Map;

public interface RenderEngine {

    String getStatement(CallCtx ctx, Map<String, Object> values);
}
