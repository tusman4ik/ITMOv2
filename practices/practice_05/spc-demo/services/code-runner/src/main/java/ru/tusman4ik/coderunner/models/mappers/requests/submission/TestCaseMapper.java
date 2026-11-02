package ru.tusman4ik.coderunner.models.mappers.requests.submission;

import ru.tusman4ik.apicontracts.coderunner.submission.*;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.CheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.FeederDto;
import ru.tusman4ik.coderunner.models.domain.group.TestCase;

import java.util.Map;

import static ru.tusman4ik.coderunner.models.mappers.ValidationUtils.shoulNotContainsPlaceholders;
import static ru.tusman4ik.coderunner.utils.CommonUtils.or;

public final class TestCaseMapper {


    private TestCaseMapper() {}

    public static TestCase fromDto(Context ctx) {
        SubmissionRequestDto submissionRequestDto = ctx.getSubmissionRequestDto();
        TestGroupDto testGroupDto = ctx.getTestGroupDto();
        TestCaseDto testCaseDto = ctx.getTestCaseDto();

        PayloadDto targetPayload = PayloadDto.propagate(
                testCaseDto.payload(),
                testGroupDto.payload(),
                submissionRequestDto.payload()
        );

        String feeder = replacePlaceholders(testGroupDto, testCaseDto, targetPayload.feeder());
        String limits = replacePlaceholders(testGroupDto, testCaseDto, targetPayload.lim());
        String checker = replacePlaceholders(testGroupDto, testCaseDto, targetPayload.checker());

        shoulNotContainsPlaceholders(".*\\{.*}.*", feeder, ctx.goTo("feeder"));
        shoulNotContainsPlaceholders(".*\\{.*}.*", limits, ctx.goTo("lim"));
        shoulNotContainsPlaceholders(".*\\{.*}.*", checker, ctx.goTo("checker"));

        FeederDto feederDto = getOrAddError(submissionRequestDto.feeders(), feeder, ctx.goTo("feeder"));
        LimitsDto limitsDto = getOrAddError(submissionRequestDto.limits(), limits, ctx.goTo("lim"));
        CheckerDto checkerDto = getOrAddError(submissionRequestDto.checkers(), checker, ctx.goTo("checker"));

        return TestCase.builder()
                .id(testCaseDto.id())
                .feeder(FeederMapper.fromDto(ctx.goTo("feeder"), feederDto))
                .lim(LimitsMapper.fromDto(ctx.goTo("lim"), limitsDto))
                .checker(CheckerMapper.fromDto(ctx.goTo("checker"), checkerDto))
                .build();
    }

    private static <T> T getOrAddError(Map<String, T> map, String key, Context ctx) {
        if (key == null) {
            return null;
        }
        if (map == null) {
            ctx.getErrors().add(String.format("%s: entity not found. got %s, map is null", ctx.getCurrentPos(), key));
            return null;
        }
        T value = map.get(key);
        if (value == null) {
            String allowedKeys = String.join(", ", map.keySet().stream().map(k -> "'" + k + "'").toList());
            ctx.getErrors().add(String.format("%s: entity not found. got %s, expected one of: [%s]", ctx.getCurrentPos(), key, allowedKeys));
        }
        return value;
    }


    private static String replacePlaceholders(
            TestGroupDto testGroupDto,
            TestCaseDto testCaseDto,
            String stringWithPlaceholder) {
        if (stringWithPlaceholder == null) {
            return null;
        }
        String tgId = or(testGroupDto.id(), "");
        String tcId = or(testCaseDto.id(), "");

        return stringWithPlaceholder
                .replace("{tg.id}", tgId)
                .replace("{tc.id}", tcId);
    }


}
