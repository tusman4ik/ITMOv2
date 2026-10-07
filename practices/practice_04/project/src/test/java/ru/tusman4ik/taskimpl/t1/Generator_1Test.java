package ru.tusman4ik.taskimpl.t1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Generator_1Test {

    @Test
    void datasetsHaveKnownAnswers() {
        assertEquals("Москва", Generator_1.build(0).answer());
        assertEquals("Байкал", Generator_1.build(1).answer());
    }

    @Test
    void byteMathHoldsIndependently() {
        // Независимая перепроверка: исходник минус вариант без ответа = ровно 8 символов
        // (КОИ-8: 1 символ = 1 байт), без использования логики build.
        String[][] lists = {
                {"Обь", "Лена", "Волга", "Москва", "Макензи", "Амазонка"},
                {"Дон", "Байкал", "Волга", "Ока"}
        };
        String[] answers = {"Москва", "Байкал"};
        for (int d = 0; d < lists.length; d++) {
            String original = "«" + String.join(", ", lists[d]) + "»";
            String deleted = answers[d];
            // Удаляем название + один соседний разделитель ", " (как ученик из условия).
            String shortened = original.replace(deleted + ", ", "").replace(", " + deleted, "");
            assertEquals(8, original.length() - shortened.length(),
                    "dataset " + d + ": delta must be 8 chars");
            assertTrue(Generator_1.build(d).statementText().contains(deleted),
                    "statement must show the original list including the answer");
        }
    }

    @Test
    void deterministicPerDataset() {
        assertEquals(Generator_1.build(0).statementText(), Generator_1.build(0).statementText());
        assertEquals(Generator_1.build(1).statementText(), Generator_1.build(1).statementText());
    }
}
