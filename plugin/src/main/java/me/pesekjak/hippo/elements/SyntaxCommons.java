package me.pesekjak.hippo.elements;

import org.intellij.lang.annotations.RegExp;

/**
 * Common patterns used in syntax.
 */
public final class SyntaxCommons {

    /**
     * Regex expression for variable names.
     */
    public static final @RegExp String VARIABLE_NAME = "^[a-zA-Z_$][a-zA-Z_$0-9]*$";

    private SyntaxCommons() {
        throw new UnsupportedOperationException();
    }

}
