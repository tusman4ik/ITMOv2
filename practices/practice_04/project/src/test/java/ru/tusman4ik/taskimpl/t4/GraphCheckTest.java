package ru.tusman4ik.taskimpl.t4;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.tusman4ik.task.registry.GeneratorRegistry;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.registry.enums.Exam;
import ru.tusman4ik.task.registry.enums.Subj;
import ru.tusman4ik.task.templates.CheckResult;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GraphCheckTest {

    @Autowired
    private GeneratorRegistry registry;

    @Test
    void rightAnswerScoresOne() {
        PrototypeInfo info = registry.info(new PrototypeId(Exam.OGE, Subj.CS, 4, 0));
        for (long seed = 2000; seed < 2010; seed++) {
            Generator_4.GraphSpec spec = Generator_4.build(new Random(seed),
                    new Generator_4.GraphConfig(5, 2, 3, 1, 5, 2, 5));
            CheckResult result = info.check(seed, "{\"answer\":" + spec.answer() + "}");
            assertEquals(1, result.total(), "seed=" + seed);
        }
    }

    @Test
    void wrongAnswerScoresZero() {
        PrototypeInfo info = registry.info(new PrototypeId(Exam.OGE, Subj.CS, 4, 0));
        for (long seed = 2000; seed < 2010; seed++) {
            Generator_4.GraphSpec spec = Generator_4.build(new Random(seed),
                    new Generator_4.GraphConfig(5, 2, 3, 1, 5, 2, 5));
            CheckResult result = info.check(seed, "{\"answer\":" + (spec.answer() + 1000) + "}");
            assertEquals(0, result.total(), "seed=" + seed);
        }
    }
}
