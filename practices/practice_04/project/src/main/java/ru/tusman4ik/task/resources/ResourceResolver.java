package ru.tusman4ik.task.resources;

import java.util.List;


public record ResourceResolver(Loader loader, String base) {

    public static final String ROOT = "spc-res";

    public static ResourceResolver root(Loader loader, String base) {
        return new ResourceResolver(loader, ROOT + "/" + base);
    }

    public String text(String name) {
        return loader.text(base + "/" + name);
    }

    public <T> List<T> yaml(String name, Class<T> type) {
        return loader.yaml(base + "/" + name, type);
    }

    public byte[] blob(String name) {
        return loader.blob(base + "/" + name);
    }
}
