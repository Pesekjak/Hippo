package me.pesekjak.hippo.classes.contents;

import ch.njol.skript.lang.Expression;
import me.pesekjak.hippo.classes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Field extends Annotatable {

    private final PrimitiveType primitiveType;
    private final Type type;
    private final String name;
    private Expression<?> value;
    private Constant constant;

    public Field(@NotNull PrimitiveType primitive, @Nullable Type type, @NotNull String name) {
        this.primitiveType = primitive;
        this.type = type;
        this.name = name;
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Expression<?> getValue() {
        return value;
    }

    public void setValue(Expression<?> value) {
        this.value = value;
    }

    public Constant getConstant() {
        return constant;
    }

    public void setConstant(Constant constant) {
        this.constant = constant;
    }

    public String getDescriptor() {
        return this.getType() != null ? this.getType().getDescriptor() : this.getPrimitiveType().getDescriptor();
    }

//    public String toJavaCode(SkriptClass skriptClass) {
//        StringBuilder stringBuilder = new StringBuilder();
//        String fieldType;
//        for(Annotation annotation : getAnnotations()) {
//            stringBuilder.append(annotation.toJavaCode()).append(" ");
//        }
//        stringBuilder.append(modifiersAsString()).append(" ");
//        if(type != null) {
//            fieldType = type.getJavaClassPath();
//        } else {
//            fieldType = primitive.getPrimitive();
//        }
//        stringBuilder.append(fieldType).append(" ");
//        stringBuilder.append(name);
//        if(constant != null || value != null) stringBuilder.append(" = ").append("(").append(fieldType).append(") ");
//        if(constant != null) {
//            stringBuilder.append(constant.toJavaCode());
//        }
//        if(value != null) {
//            stringBuilder.append(SkriptClassRegistry.getJavaCodeForClass(skriptClass.getClassName()))
//            .append(".getField(\"").append(this.name).append("\").")
//            .append("getValue()").append(".getSingle(")
//            .append(SkriptClassRegistry.getJavaCodeForClass(skriptClass.getClassName())).append(".getDefineEvent()")
//            .append(")");
//        }
//        stringBuilder.append(";");
//        return stringBuilder.toString();
//    }

}
