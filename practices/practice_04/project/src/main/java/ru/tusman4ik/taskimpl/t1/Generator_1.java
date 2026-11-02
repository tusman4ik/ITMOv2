package ru.tusman4ik.taskimpl.t1;

import ru.tusman4ik.task.generators.PrototypeGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * База прототипов семейства 01 (кодировки, вычёркивание из списка).
 *
 * <p>Выбор слота задокументирован: {@code number = 1} — семейство 01
 * (рядом лежит {@code 01-00} языковой вариант без генератора);
 * {@code prototype = 1} — новый вариант {@code 01-01}, шаблонный текст отличается
 * (КОИ-8, реки, −8 байт), поэтому это отдельный прототип, а не вариант шаблона.
 * {@code exam = EGE} — задачи на кодировки такого вида относятся к ЕГЭ.
 *
 * <p>Изменяемый параметр — {@code datasetIdx} (набор списка). Байтовая математика
 * фиксирована: КОИ-8 = 1 байт/символ, удаление среднего элемента убирает
 * {@code len + 2} символа (название + ", "), крайнего — тоже {@code len + 2}.
 * Поэтому при {@code DELTA_BYTES = 8} валидно удаление только названия из 6 букв.
 * Каждый датасет содержит РОВНО ОДНО такое название (проверяется в build и тесте);
 * seed влияет только на выбор датасета через {@code @PickIntNodeDef}, ответ внутри
 * датасета детерминирован байтовой математикой.
 */
public abstract class Generator_1 extends PrototypeGenerator {

    static final int DELTA_BYTES = 8;

    record Dataset(String kind, List<String> items) {}

    static final List<Dataset> DATASETS = List.of(
            new Dataset("реки", List.of("Обь", "Лена", "Волга", "Москва", "Макензи", "Амазонка")),
            new Dataset("озёра", List.of("Дон", "Байкал", "Волга", "Ока"))
    );

    public record RiverTask(String statementText, String answer) {}

    public static RiverTask build(int datasetIdx) {
        if (datasetIdx < 0 || datasetIdx >= DATASETS.size()) {
            throw new IllegalArgumentException("datasetIdx out of range [0, " + DATASETS.size() + "): " + datasetIdx);
        }
        Dataset ds = DATASETS.get(datasetIdx);
        List<String> valid = new ArrayList<>();
        for (String name : ds.items()) {
            if (name.length() + 2 == DELTA_BYTES) {
                valid.add(name);
            }
        }
        if (valid.size() != 1) {
            throw new IllegalStateException("dataset " + datasetIdx + " must have exactly one 6-letter item, got: " + valid);
        }
        String deleted = valid.get(0);
        // Ученик видит ИСХОДНЫЙ список (с удалённой рекой на месте) и считает разницу.
        String list = "«" + String.join(", ", ds.items()) + " — " + ds.kind() + "»";
        String statementText = "В кодировке КОИ-8 каждый символ кодируется 8 битами. "
                + "Андрей написал текст (в нем нет лишних пробелов):\n\n"
                + list + ".\n\n"
                + "Ученик вычеркнул из списка название одной из " + riverWord(ds.kind())
                + ". Заодно он вычеркнул ставшие лишними запятые и пробелы"
                + " — два пробела не должны идти подряд.\n\n"
                + "При этом размер нового предложения в данной кодировке оказался на 8 байтов меньше, "
                + "чем размер исходного предложения. Напишите в ответе вычеркнутое название "
                + singular(ds.kind()) + ".";
        return new RiverTask(statementText, deleted);
    }

    private static String riverWord(String kind) {
        return kind.equals("реки") ? "рек" : "озёр";
    }

    private static String singular(String kind) {
        return kind.equals("реки") ? "реки" : "озера";
    }
}
