package ru.tusman4ik.task.templates;

import com.samskivert.mustache.Mustache;

import java.util.Map;

public class MustacheRenderEngine implements RenderEngine {

    private static final Mustache.Compiler COMPILER = Mustache.compiler();

    @Override
    public String getStatement(CallCtx ctx, Map<String, Object> values) {
        String text = ctx.res().tmpl().text("statement.mustache");
        return COMPILER.compile(text).execute(values);
    }
}
