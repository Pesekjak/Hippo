package me.pesekjak.hippo.core.annotations;

import me.pesekjak.hippo.core.ASMUtil;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.Objects;

public record ClassLiteral(Type type) implements AnnotationVisitable {

    public ClassLiteral {
        Objects.requireNonNull(type);
    }

    public ClassLiteral(String typeDescriptor) {
        this(Type.getType(typeDescriptor));
    }

    public static ClassLiteral fromDotPath(String dotPath) {
        return new ClassLiteral(ASMUtil.getDescriptor(dotPath));
    }

    @Override
    public void visit(@Nullable String name, AnnotationVisitor visitor) {
        visitor.visit(name, visitor);
    }

}
