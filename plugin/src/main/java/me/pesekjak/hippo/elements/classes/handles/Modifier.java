package me.pesekjak.hippo.elements.classes.handles;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrappers for Java modifiers.
 */
public enum Modifier {

    PUBLIC(ACC_PUBLIC),
    PRIVATE(ACC_PRIVATE),
    PROTECTED(ACC_PROTECTED),
    STATIC(ACC_STATIC),
    FINAL(ACC_FINAL),
    SYNCHRONIZED(ACC_SYNCHRONIZED),
    VOLATILE(ACC_VOLATILE),
    TRANSIENT(ACC_TRANSIENT),
    NATIVE(ACC_NATIVE),
    ABSTRACT(ACC_ABSTRACT),
    STRICT(ACC_STRICT),
    DEFAULT(0);

    private final int value;

    Modifier(int value) {
        this.value = value;
    }

    /**
     * @return opcode for the modifier
     */
    public int getValue() {
        return value;
    }

    /**
     * Merges multiple modifiers together and returns their opcode.
     *
     * @param modifiers modifiers
     * @return value
     */
    public static int getModifier(Modifier... modifiers) {
        int value = 0;
        for (Modifier modifier : modifiers)
            value |= modifier.getValue();
        return value;
    }

}
