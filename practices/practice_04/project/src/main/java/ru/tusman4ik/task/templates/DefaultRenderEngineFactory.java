package ru.tusman4ik.task.templates;

public final class DefaultRenderEngineFactory {

    public static RenderEngine get(){
        return new MustacheRenderEngine();
    }
}
