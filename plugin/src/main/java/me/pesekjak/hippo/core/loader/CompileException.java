package me.pesekjak.hippo.core.loader;

import org.objectweb.asm.Type;

/**
 * Exception thrown because of existence of illegal class during pre-compilation.
 */
public class CompileException extends Exception {

    public static final String NOT_EXISTING = "Class does not exist";

    //
    // Extending other classes
    //
    public static final String EXTENDS_FINAL = "Class '%s' cannot extend final class";
    public static final String EXTENDS_INTERFACE = "Class '%s' cannot extend interface class";
    public static final String EXTENDS_NON_EXISTING = "Class '%s' extends non-existing class";

    //
    // Implementing other classes
    //
    public static final String IMPLEMENTS_NON_INTERFACE = "Class '%s' cannot implement a class that is not an interface";
    public static final String IMPLEMENTS_NON_EXISTING = "Class '%s' extends non-existing class";

    //
    // Inheritance issues
    //
    public static final String CYCLIC_INHERITANCE = "Class '%s' cannot be compiled due to cyclic inheritance";

    public CompileException() {
        super();
    }

    public CompileException(String message, Type type) {
        super(message.formatted(type.getClassName()));
    }

    public CompileException(String message, Type type, Throwable cause) {
        super(message.formatted(type.getClassName()), cause);
    }

    public CompileException(Throwable cause) {
        super(cause);
    }


}
