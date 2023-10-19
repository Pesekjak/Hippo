package me.pesekjak.hippo.core;

import static org.objectweb.asm.Opcodes.*;

/**
 * Util class for storing constant values related to modifiers.
 */
public final class Modifiers {

    private static final int CLASS_MODIFIERS = ACC_PUBLIC | ACC_PRIVATE
            | ACC_PROTECTED | ACC_STATIC | ACC_FINAL | ACC_SUPER | ACC_INTERFACE
            | ACC_ABSTRACT | ACC_STRICT | ACC_SYNTHETIC | ACC_ANNOTATION | ACC_RECORD | ACC_ENUM;

    private static final int INTERFACE_MODIFIERS = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_ABSTRACT | ACC_STATIC | ACC_STRICT;

    private static final int CONSTRUCTOR_MODIFIERS = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED;

    private static final int METHOD_MODIFIERS = ACC_PUBLIC | ACC_PRIVATE
            | ACC_PROTECTED | ACC_STATIC | ACC_FINAL | ACC_SYNCHRONIZED | ACC_BRIDGE
            | ACC_VARARGS | ACC_NATIVE | ACC_ABSTRACT | ACC_STRICT | ACC_SYNTHETIC;

    private static final int FIELD_MODIFIERS = ACC_PUBLIC | ACC_PRIVATE
            | ACC_PROTECTED | ACC_STATIC | ACC_FINAL | ACC_VOLATILE | ACC_TRANSIENT
            | ACC_SYNTHETIC | ACC_ENUM;

    private static final int ACCESS_MODIFIERS = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED;

    private Modifiers() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return modifiers applicable to classes
     */
    public static int getClassModifiers() {
        return CLASS_MODIFIERS;
    }

    /**
     * @return modifiers applicable to interfaces
     */
    public static int getInterfaceModifiers() {
        return INTERFACE_MODIFIERS;
    }

    /**
     * @return modifiers applicable to constructors
     */
    public static int getConstructorModifiers() {
        return CONSTRUCTOR_MODIFIERS;
    }

    /**
     * @return modifiers applicable to methods
     */
    public static int getMethodModifiers() {
        return METHOD_MODIFIERS;
    }

    /**
     * @return modifiers applicable to fields
     */
    public static int getFieldModifiers() {
        return FIELD_MODIFIERS;
    }

    /**
     * @return access modifiers
     */
    public static int getAccessModifiers() {
        return ACCESS_MODIFIERS;
    }

}
