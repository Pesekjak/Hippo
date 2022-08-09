package me.pesekjak.hippo.classes;

import lombok.Getter;
import org.objectweb.asm.Opcodes;

public enum ClassType {

    CLASS(0),
    INTERFACE(Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT),
    RECORD( Opcodes.ACC_FINAL | Opcodes.ACC_SUPER | Opcodes.ACC_RECORD),
    ENUM(Opcodes.ACC_FINAL | Opcodes.ACC_SUPER | Opcodes.ACC_ENUM),
    ANNOTATION(Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_ANNOTATION);

    @Getter
    private final int value;

    ClassType(int value) {
        this.value = value;
    }

}
