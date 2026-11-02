package ru.tusman4ik.coderunner.models.mappers.requests.submission;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.CheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.ExecutableCheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.TokenCheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.checker.Checker;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.checker.ExecutableChecker;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.checker.TokenChecker;
import ru.tusman4ik.coderunner.models.mappers.IContext;

import static ru.tusman4ik.coderunner.utils.CommonUtils.mergeNullableCollection;

public final class CheckerMapper {
    private CheckerMapper() {}

    @Nullable
    public static Checker fromDto(@NonNull IContext ctx, @Nullable CheckerDto dto) {
        if (dto == null) {
            return null;
        }
        return switch (dto) {
            case ExecutableCheckerDto execDto -> ExecutableChecker.builder()
                    .lim(LimitsMapper.fromDto(ctx.goTo("lim"), execDto.lim()))
                    .args(mergeNullableCollection(execDto.args(), execDto.additionalArgs()))
                    .internalName(execDto.internalName())
                    .build();
            case TokenCheckerDto tokenDto -> TokenChecker.builder()
                    .caseSensitive(tokenDto.caseSensitive())
                    .numericTolerance(tokenDto.numericTolerance())
                    .regexp(tokenDto.regexp())
                    .build();
            default -> throw new IllegalArgumentException("Unknown CheckerDto type: " + dto.getClass());
        };
    }
}
