package me.pesekjak.hippo.core.loader;

import me.pesekjak.hippo.core.AbstractClass;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a written class.
 *
 * @param clazz class source
 * @param data data of the class
 */
public record ClassSignature(AbstractClass clazz, byte[] data) {

    public ClassSignature(AbstractClass clazz) {
        this(clazz, null);
    }

    public ClassSignature {
        Objects.requireNonNull(clazz);
        if (data == null) data = createData(clazz);
    }

    /**
     * Creates data for the class.
     *
     * @param clazz class to create the data for
     * @return class data
     */
    private static byte[] createData(AbstractClass clazz) {
        ClassWriter writer = new ClassWriter(Opcodes.ASM9 | ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        clazz.visit(writer);
        return writer.toByteArray();
    }

    /**
     * Checks whether this signature matches other one.
     *
     * @param other other signature
     * @return whether they are the same
     */
    public boolean matches(ClassSignature other) {
        if (!clazz.getName().equals(other.clazz.getName())) return false;
        return Arrays.equals(data, other.data);
    }

    @Override
    public String toString() {
        return "ClassSignature(" + clazz.getName() + ")";
    }

}
