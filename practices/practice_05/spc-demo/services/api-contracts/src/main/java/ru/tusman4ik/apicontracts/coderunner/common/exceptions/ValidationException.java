package ru.tusman4ik.apicontracts.coderunner.common.exceptions;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

public class ValidationException extends RuntimeException {

    public ValidationException(@NonNull List<String> errors) {
        super(String.format("Validation error: %d items", errors.size()));
        this.errors = errors;
    }

    @Getter
    private final List<String> errors;
}