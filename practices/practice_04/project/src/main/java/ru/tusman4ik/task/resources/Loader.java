package ru.tusman4ik.task.resources;

import java.util.List;

/**
 * Сырое чтение ресурсов по строковой classpath-локации
 * ({@code tasks/01-01/templates/02/statement.mustache}).
 * Владелец кеша, синглтон инфры. Баз не знает: они в {@link ResourceResolver}.
 */
public interface Loader {

    String text(String location);

    <T> List<T> yaml(String location, Class<T> type);

    byte[] blob(String location);
}
