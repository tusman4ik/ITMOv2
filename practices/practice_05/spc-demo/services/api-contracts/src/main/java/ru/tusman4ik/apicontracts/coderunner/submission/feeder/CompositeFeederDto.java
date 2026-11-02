package ru.tusman4ik.apicontracts.coderunner.submission.feeder;

import lombok.Builder;

import java.util.List;

import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.or;

@Builder
public record CompositeFeederDto(
        List<FeederDto> feeders
) implements FeederDto {

    public static final String DISC = "composite";

    public CompositeFeederDto {
        feeders = or(feeders, List.of());
    }

}
