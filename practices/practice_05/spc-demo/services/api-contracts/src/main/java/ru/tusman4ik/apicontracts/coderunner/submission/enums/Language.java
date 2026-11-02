package ru.tusman4ik.apicontracts.coderunner.submission.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Programming languages supported for source code submission.
 * <p>
 * Each language is supported by the evaluation system with appropriate
 * compiler/interpreter configuration.
 * 
 *
 * @author tusman4ik
 * @since 0.2.0-alpha
 */
@Schema(description = "Programming language of the submitted source code")
public enum Language {

    /**
     * C++ programming language.
     */
    CPP,

    /**
     * C# programming language.
     */
    CSHARP,

    /**
     * Java programming language.
     */
    JAVA,

    /**
     * Pascal programming language.
     */
    PASCAL,

    /**
     * Python programming language.
     */
    PYTHON
}