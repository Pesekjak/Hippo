package me.pesekjak.hippo.classes.types.primitives;

import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.types.PrimitiveType;
import org.objectweb.asm.Opcodes;

public class DoubleType extends PrimitiveType {

    protected DoubleType(String descriptor) {
        super(descriptor, Primitive.DOUBLE);
    }

    public DoubleType() {
        super(Primitive.DOUBLE);
    }

    @Override
    public DoubleType array() {
        return new DoubleType("[" + descriptor);
    }

    @Override
    public int loadCode() {
        if (isArray()) return Opcodes.ALOAD;
        return Opcodes.DLOAD;
    }

    @Override
    public int storeCode() {
        if (isArray()) return Opcodes.ASTORE;
        return Opcodes.DSTORE;
    }

    @Override
    public int returnCode() {
        if (isArray()) return Opcodes.ARETURN;
        return Opcodes.DRETURN;
    }

    @Override
    public int size() {
        return isArray() ? 1 : 2;
    }

}
