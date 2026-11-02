package ru.tusman4ik.coderunner.models.mappers.requests.submission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.*;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.*;
import ru.tusman4ik.coderunner.models.mappers.IContext;

import static ru.tusman4ik.coderunner.models.mappers.ValidationUtils.shouldBeNotBlank;
import static ru.tusman4ik.coderunner.utils.CommonUtils.mergeNullableCollection;

public final class FeederMapper {
    private FeederMapper() {}

    @Nullable
    public static Feeder fromDto(@NotNull IContext ctx, @Nullable FeederDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto) {
            case CompositeFeederDto compositeDto -> CompositeFeeder.builder()
                    .feeders(
                            compositeDto.feeders().stream()
                                    .map(f -> fromDto(ctx.goTo("feeders"), f))
                                    .toList()
                    )
                    .build();
            case TextFeederDto textDto -> {
                shouldBeNotBlank(textDto.stdin(), ctx.goTo("stdin"));
                yield TextFeeder.builder()
                        .stdin(textDto.stdin())
                        .target(textDto.target())
                        .build();
            }
            case FileFeederDto fileDto -> {
                shouldBeNotBlank(fileDto.path(), ctx.goTo("path"));
                yield FileFeeder.builder()
                        .path(fileDto.path())
                        .target(fileDto.target())
                        .build();
            }
            case ExecutableFeederDto execDto -> ExecutableFeeder.builder()
                    .lim(LimitsMapper.fromDto(ctx.goTo("lim"), execDto.lim()))
                    .args(mergeNullableCollection(execDto.args(), execDto.additionalArgs()))
                    .internalName(execDto.internalName())
                    .build();
            default -> throw new IllegalArgumentException("Unknown FeederDto type: " + dto.getClass());
        };
    }
}
