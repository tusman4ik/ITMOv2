package ru.tusman4ik.taskservice.taskimpl.t16;

import org.junit.jupiter.api.Test;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;
import ru.tusman4ik.taskservice.task.id.InstanceId;
import ru.tusman4ik.taskservice.task.id.PrototypeId;
import ru.tusman4ik.taskservice.task.node.Node;
import ru.tusman4ik.taskservice.taskimpl.t16.common.NumberSequenceUtils;
import ru.tusman4ik.taskservice.taskimpl.t16.common.TestCase;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Generator_16_002_Test {

    @Test
    void testNodesGenerationAndReferenceCorrectness() throws Exception {
        Generator_16_002 generator = new Generator_16_002(null, "http://localhost:8080");
        TestContextRunner.memoizeNodes(generator);

        PrototypeId prototypeId = new PrototypeId(Exam.OGE, Subject.CS, 16, 2);
        InstanceId instanceId = InstanceId.builder()
                .prototypeId(prototypeId)
                .seed(54321L)
                .fingerprint(0L)
                .build();

        TestContextRunner.runInContext(instanceId, () -> {
            try {
                Node<Integer> baseNode = getNode(generator, "base");
                Node<Integer> digitNode = getNode(generator, "digit");
                Node<List<TestCase>> testsNode = getNode(generator, "tests");
                Node<String> firstExampleIn = getNode(generator, "firstExampleIn");
                Node<String> firstExampleExp = getNode(generator, "firstExampleExp");
                Node<String> secondExampleIn = getNode(generator, "secondExampleIn");
                Node<String> secondExampleExp = getNode(generator, "secondExampleExp");

                int base = baseNode.get();
                int digit = digitNode.get();
                List<TestCase> tests = testsNode.get();

                assertThat(base).isBetween(3, 8);
                assertThat(digit).isBetween(0, base - 1);
                assertThat(tests).isNotEmpty().hasSizeGreaterThanOrEqualTo(10);

                assertThat(firstExampleIn.get()).isEqualTo(tests.get(0).in());
                assertThat(firstExampleExp.get()).isEqualTo(tests.get(0).expected());
                assertThat(secondExampleIn.get()).isEqualTo(tests.get(1).in());
                assertThat(secondExampleExp.get()).isEqualTo(tests.get(1).expected());

                // Verify reference solving correctness for each testcase
                for (TestCase testCase : tests) {
                    long[] numbers = Arrays.stream(testCase.in().split("\n"))
                            .mapToLong(Long::parseLong)
                            .toArray();
                    long[] trimmed = Arrays.copyOf(numbers, numbers.length - 1);
                    long expectedSum = NumberSequenceUtils.sumPenultimateDigitEquals(trimmed, base, digit);
                    assertThat(testCase.expected()).isEqualTo(String.valueOf(expectedSum));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> Node<T> getNode(Object generator, String fieldName) throws Exception {
        Field field = generator.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Node<T>) field.get(generator);
    }
}
