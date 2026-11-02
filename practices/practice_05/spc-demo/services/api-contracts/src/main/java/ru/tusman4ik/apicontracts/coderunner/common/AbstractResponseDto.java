package ru.tusman4ik.apicontracts.coderunner.common;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.ResponseStatus;
import ru.tusman4ik.apicontracts.coderunner.common.exceptions.ValidationException;

import java.util.List;

@Slf4j
public record AbstractResponseDto<T>(
        ResponseStatus status, String message, T data, List<String> errors
) {

    public static <T> AbstractResponseDto<T> success(T data) {
        return new AbstractResponseDto<>(ResponseStatus.SUCCESS, null, data, null);
    }

    public static AbstractResponseDto<Void> validationError(ValidationException ex) {
        log.error("Error: ", ex);
        return new AbstractResponseDto<>(ResponseStatus.VALIDATION_ERROR, ex.getMessage(), null, ex.getErrors());
    }

    public static AbstractResponseDto<Void> systemError(RuntimeException ex) {
        log.error("Error: ", ex);
        return new AbstractResponseDto<>(ResponseStatus.INTERNAL_ERROR, ex.getMessage(), null, null);
    }

    public static AbstractResponseDto<Void> jsonError(JacksonException ex) {
        log.error("Error: ", ex);
        String userFriendlyMessage = "Invalid JSON payload";

        if (ex instanceof InvalidFormatException ife) {
            userFriendlyMessage = String.format("Invalid format for field '%s': cannot convert value '%s' to target type %s",
                    ife.getPathReference(), ife.getValue(), ife.getTargetType().getSimpleName());
        } else if (ex instanceof MismatchedInputException mie) {
            userFriendlyMessage = String.format("Mismatched input at '%s': %s",
                    mie.getPathReference(), mie.getOriginalMessage());
        }

        return new AbstractResponseDto<>(
                ResponseStatus.PARSING_ERROR,
                userFriendlyMessage,
                null,
                List.of(ex.getOriginalMessage())
        );
    }
}