package me.pesekjak.hippo.core.annotations;

import me.pesekjak.hippo.core.ASMUtil;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Represents a reference to enum constant.
 *
 * @param type enum type
 * @param name name of the constant
 */
public record EnumConstant(Type type, String name) implements AnnotationVisitable {

    public EnumConstant {
        Objects.requireNonNull(type);
        Objects.requireNonNull(name);
    }

    public EnumConstant(String typeDescriptor, String name) {
        this(Type.getType(typeDescriptor), name);
    }

    /**
     * Creates enum constant from a dot path and its name.
     *
     * @param dotPath dot path of the enum type
     * @param name name of the constant
     * @return
     */
    public static EnumConstant fromDotPath(String dotPath, String name) {
        return new EnumConstant(ASMUtil.getDescriptor(dotPath), name);
    }

    @Override
    public void visit(@Nullable String name, AnnotationVisitor visitor) {
        visitor.visitEnum(name, type().getDescriptor(), name);
    }

}
