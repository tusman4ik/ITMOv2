package ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder;

import lombok.Builder;

import java.util.List;

@Builder
public record CompositeFeeder(
     List<Feeder> feeders
)  implements Feeder {
}
