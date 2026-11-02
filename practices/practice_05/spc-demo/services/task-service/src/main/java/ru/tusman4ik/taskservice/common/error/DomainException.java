package ru.tusman4ik.taskservice.common.error;

import lombok.Getter;

public abstract class DomainException extends RuntimeException {

    @Getter
    private final ExceptionDef exceptionDef;

    @Getter
    private final Object params;

    @Getter
    private final String detail;

    protected DomainException(ExceptionDef exceptionDef, Object params, String detail) {
        super(exceptionDef.code() + ": " + params);
        this.exceptionDef = exceptionDef;
        this.params = params;
        this.detail = detail;
    }


}
