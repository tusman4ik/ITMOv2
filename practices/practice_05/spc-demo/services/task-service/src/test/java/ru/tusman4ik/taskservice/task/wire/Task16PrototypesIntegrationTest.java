package ru.tusman4ik.taskservice.task.wire;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;
import ru.tusman4ik.taskservice.task.id.PrototypeId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Task16PrototypesIntegrationTest {

    @Autowired
    private GeneratorRegistry generatorRegistry;

    @Test
    void shouldContainAllExpectedPrototypes() {
        List<Integer> expectedIndices = List.of(1, 2, 3, 4, 5, 999);

        for (int idx : expectedIndices) {
            PrototypeId id = new PrototypeId(Exam.OGE, Subject.CS, 16, idx);
            assertThat(generatorRegistry.contains(id))
                    .as("GeneratorRegistry should contain prototype 16-%03d", idx)
                    .isTrue();
            assertThat(generatorRegistry.getGenerator(id))
                    .as("Generator for 16-%03d should not be null", idx)
                    .isNotNull();
        }
    }
}
