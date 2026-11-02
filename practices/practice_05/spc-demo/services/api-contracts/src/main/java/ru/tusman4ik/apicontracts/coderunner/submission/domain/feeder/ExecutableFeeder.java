package ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.limits.Limits;

import java.util.List;

@Builder
@JsonDeserialize(builder = ExecutableFeeder.ExecutableFeederBuilder.class)
public record ExecutableFeeder(
        Limits lim,
        List<String> args,
        String internalName
) implements Feeder {
}

