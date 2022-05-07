package me.pesekjak.hippo.classes;

import org.objectweb.asm.Opcodes;

public enum ClassType {

    CLASS("class", 0),
    INTERFACE("interface", Opcodes.ACC_INTERFACE + Opcodes.ACC_ABSTRACT),
    RECORD("record", Opcodes.ACC_RECORD),
    ENUM("enum", Opcodes.ACC_FINAL + Opcodes.ACC_SUPER + Opcodes.ACC_ENUM),
    ANNOTATION("annotation", Opcodes.ACC_ANNOTATION  + Opcodes.ACC_INTERFACE + Opcodes.ACC_ABSTRACT);

    public String identifier;
    public int value;

    ClassType(String identifier, int value) {
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
