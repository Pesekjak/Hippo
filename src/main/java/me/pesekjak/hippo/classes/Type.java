package me.pesekjak.hippo.classes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents Hippo Type
 */
public interface Type {

    /**
     * Returns dot path of the Type, can be null if primitive.
     * @return Dot path of the Type
     */
    @Nullable String dotPath();

    /**
     * Returns descriptor of the Type.
     * @return descriptor of the Type.
     */
    @NotNull String descriptor();

    /**
     * Returns internal name of the Type, can be null if primitive.
     * @return internal name of the Type
     */
    @Nullable String internalName();

    /**
     * Returns simple, easy to understand name of the Type used in Skript errors.
     * @return Simple name of the Type
     */
    @NotNull String simpleName();

    /**
     * Returns array Type of this Type.
     * @return array Type of this Type.
     */
    Type array();

    boolean isArray();

    /**
     * Returns load OpCode for this Type.
     * @return load OpCode for this Type
     */
    int loadCode();

    /**
     * Returns store OpCode for this Type.
     * @return store OpCode of this Type
     */
    int storeCode();

    /**
     * Returns OpCode for returning variable of this Type.
     * @return OpCode for returning variable of this Type.
     */
    int returnCode();

    /**
     * Returns number of slots the datatype requires on the stack.
     * @return number of slots the datatype requires on the stack
     */
    int size();

    /**
     * Converts Hippo's Type to ASM Type
     * @return ASM Type of the this Type
     */
    org.objectweb.asm.Type toASM();

    /**
     * Tries to find the class of this Type, null if doesn't exist.
     * @return Class of this Type
     */
    @Nullable Class<?> findClass();

}