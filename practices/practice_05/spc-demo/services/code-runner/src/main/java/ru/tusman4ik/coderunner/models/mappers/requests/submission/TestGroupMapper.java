package ru.tusman4ik.coderunner.models.mappers.requests.submission;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import ru.tusman4ik.apicontracts.coderunner.submission.TestCaseDto;
import ru.tusman4ik.apicontracts.coderunner.submission.TestGroupDto;
import ru.tusman4ik.coderunner.models.domain.group.TestCase;
import ru.tusman4ik.coderunner.models.domain.group.TestGroup;

import java.util.List;

import static ru.tusman4ik.coderunner.models.mappers.ValidationUtils.*;

public final class TestGroupMapper {
    private TestGroupMapper() {}

    public static TestGroup fromDto(@NonNull Context ctx) {
        TestGroupDto testGroupDto = ctx.getTestGroupDto();

        shouldBeNotBlank(testGroupDto.id(), ctx.goTo("id"));
        shouldNotBeEmpty(testGroupDto.tests(), ctx.goTo("tests"));

        return TestGroup
                .builder()
                .id(testGroupDto.id())
                .tests(mapCases(ctx))
                .build();
    }

    @Nullable
    private static List<TestCase> mapCases(Context ctx) {
        TestGroupDto testGroupDto = ctx.getTestGroupDto();

        if (testGroupDto.tests() == null || testGroupDto.tests().isEmpty()) return List.of();

        return testGroupDto
                .tests()
                .stream()
                .map(tc -> mapCase(ctx, tc))
                .toList();
    }

    private static TestCase mapCase(Context ctx, TestCaseDto tc) {
        ctx.setTestCaseDto(tc);
        String nextPart = String.format("case[%s]", tc.id());
        return TestCaseMapper.fromDto(ctx.goTo(nextPart));
    }
}
