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
    private ConstantArray constantArray;

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

    public ConstantArray getConstantArray() {
        return constantArray;
    }

    public void setConstantArray(ConstantArray constantArray) {
        this.constantArray = constantArray;
    }

    public String getDescriptor() {
        return this.getType() != null ? this.getType().getDescriptor() : this.getPrimitiveType().getDescriptor();
    }

}
