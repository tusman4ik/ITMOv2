package ru.tusman4ik.apicontracts.coderunner.callback.enums;

public enum Status {
    OK,     // Accepted
    TL,     // Time limit
    ML,     // Memory limit
    CE,     // Compilation error
    SE,     // Syntax error
    RE,     // Runtime error
    WA,     // Wrong answer
    OL,     // Output limit (file size)
    IE,     // Internal limit
}
