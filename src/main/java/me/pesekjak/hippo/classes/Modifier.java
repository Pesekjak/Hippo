package me.pesekjak.hippo.classes;

import lombok.Getter;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum Modifier {

    PUBLIC(Opcodes.ACC_PUBLIC, true),
    PRIVATE(Opcodes.ACC_PRIVATE, true),
    PROTECTED(Opcodes.ACC_PROTECTED, true),
    FINAL(Opcodes.ACC_FINAL, false),
    STATIC(Opcodes.ACC_STATIC, false),
    ABSTRACT(Opcodes.ACC_ABSTRACT, false),
    TRANSIENT(Opcodes.ACC_TRANSIENT, false),
    SYNCHRONIZED(Opcodes.ACC_SYNCHRONIZED, false),
    VOLATILE(Opcodes.ACC_VOLATILE, false),
    NATIVE(Opcodes.ACC_NATIVE, false),
    STRICTFP(Opcodes.ACC_STRICT, false),
    DEFAULT(0, true);

    @Getter
    private final int value;
    @Getter
    private final boolean access;

    Modifier(int value, boolean access) {
        this.value = value;
        this.access = access;
    }

    /**
     * Checks if provided list contains more than 1 access modifier.
     * @param modifiers list of modifiers to check
     * @return true if list contains more than 1 access modifier
     */
    public static boolean accessConflict(List<Modifier> modifiers) {
        int i = 0;
        for(Modifier modifier : modifiers) {
            if(modifier.access) i++;
        }
        return i > 1;
    }

    /**
     * Checks if provided list has the same modifier more than once.
     * @param modifiers list of modifiers to check
     * @return true if list contains the same modifier more than once
     */
    public static boolean duplicates(List<Modifier> modifiers) {
        final Set<Modifier> set = new HashSet<>(modifiers);
        return modifiers.size() != set.size();
    }

}
