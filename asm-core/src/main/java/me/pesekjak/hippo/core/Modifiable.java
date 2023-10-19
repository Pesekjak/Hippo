package me.pesekjak.hippo.core;

import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_RECORD;

/**
 * Represents a content that can contains modifiers.
 */
public interface Modifiable {

    int getModifier();

    int getCompatibleModifiers();

    default boolean isPublic() {
        return (getModifier() & ACC_PUBLIC) != 0;
    }

    default boolean isPrivate() {
        return (getModifier() & ACC_PRIVATE) != 0;
    }

    default boolean isProtected() {
        return (getModifier() & ACC_PROTECTED) != 0;
    }

    default boolean isStatic() {
        return (getModifier() & ACC_STATIC) != 0;
    }

    default boolean isFinal() {
        return (getModifier() & ACC_FINAL) != 0;
    }

    default boolean isSuper() {
        return (getModifier() & ACC_SUPER) != 0;
    }

    default boolean isSynchronized() {
        return (getModifier() & ACC_SYNCHRONIZED) != 0;
    }

    default boolean isVolatile() {
        return (getModifier() & ACC_VOLATILE) != 0;
    }

    default boolean isTransient() {
        return (getModifier() & ACC_TRANSIENT) != 0;
    }

    default boolean isNative() {
        return (getModifier() & ACC_NATIVE) != 0;
    }

    default boolean isInterface() {
        return (getModifier() & ACC_INTERFACE) != 0;
    }

    default boolean isAbstract() {
        return (getModifier() & ACC_ABSTRACT) != 0;
    }

    default boolean isStrict() {
        return (getModifier() & ACC_STRICT) != 0;
    }

    default boolean isBridge() {
        return (getModifier() & ACC_BRIDGE) != 0;
    }

    default boolean isVarArgs() {
        return (getModifier() & ACC_VARARGS) != 0;
    }

    default boolean isSynthetic() {
        return (getModifier() & ACC_SYNTHETIC) != 0;
    }

    default boolean isAnnotation() {
        return (getModifier() & ACC_ANNOTATION) != 0;
    }

    default boolean isEnum() {
        return (getModifier() & ACC_ENUM) != 0;
    }

    default boolean isMandated() {
        return (getModifier() & ACC_MANDATED) != 0;
    }

    default boolean isRecord() {
        return (getModifier() & ACC_RECORD) != 0;
    }

    /**
     * Checks whether the content has legal combination of modifiers.
     *
     * @param modifiable modifiable content
     * @param name name of the content (used for debug messages)
     * @throws IllegalModifiersException if the combination is illegal
     */
    static void checkModifiers(Modifiable modifiable, String name) throws IllegalModifiersException {
        if ((modifiable.getModifier() & modifiable.getCompatibleModifiers()) != modifiable.getModifier()) {
            int incompatible = modifiable.getModifier() & ~modifiable.getCompatibleModifiers();
            throw new IllegalModifiersException(name + " contains incompatible modifiers " + Modifier.toString(incompatible).replaceAll(" ", ", "));
        }

        if (modifiable.isPublic() && modifiable.isPrivate() && modifiable.isProtected())
            throw new IllegalModifiersException(name + " contains public, protected, private modifiers at the same time");

        if (modifiable.isPublic() && modifiable.isPrivate())
            throw new IllegalModifiersException(name + " contains public, private modifiers at the same time");

        if (modifiable.isPublic() && modifiable.isProtected())
            throw new IllegalModifiersException(name + " contains public, protected modifiers at the same time");

        if (modifiable.isPrivate() && modifiable.isProtected())
            throw new IllegalModifiersException(name + " contains private, protected modifiers at the same time");
    }

}
