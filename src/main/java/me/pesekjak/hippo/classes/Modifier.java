package me.pesekjak.hippo.classes;

import org.objectweb.asm.Opcodes;

public enum Modifier {

    PUBLIC("public", Opcodes.ACC_PUBLIC),
    PRIVATE("private", Opcodes.ACC_PRIVATE),
    PROTECTED("protected", Opcodes.ACC_PROTECTED),
    FINAL("final", Opcodes.ACC_FINAL),
    STATIC("static", Opcodes.ACC_STATIC),
    ABSTRACT("abstract", Opcodes.ACC_ABSTRACT),
    TRANSIENT("transient", Opcodes.ACC_TRANSIENT),
    SYNCHRONIZED("synchronized", Opcodes.ACC_SYNCHRONIZED),
    VOLATILE("volatile", Opcodes.ACC_VOLATILE),
    NATIVE("native", Opcodes.ACC_NATIVE),
    STRICTFP("strictfp", Opcodes.ACC_STRICT);

    public String identifier;
    public int value;

    Modifier(String identifier, int value) {
        this.identifier = identifier;
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getValue() {
        return value;
    }

}
