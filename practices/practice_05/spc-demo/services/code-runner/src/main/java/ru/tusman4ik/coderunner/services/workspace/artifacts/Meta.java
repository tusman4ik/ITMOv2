package ru.tusman4ik.coderunner.services.workspace.artifacts;

import lombok.Builder;
import lombok.Singular;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;

import java.util.List;

@Builder
public record Meta(
        Mark mark,
        @Singular
        List<Stage> stages
) {
}
