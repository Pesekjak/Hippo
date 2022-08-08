package me.pesekjak.hippo.classes.types.primitives;

import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.types.PrimitiveType;
import org.objectweb.asm.Opcodes;

public class FloatType extends PrimitiveType {

    protected FloatType(String descriptor) {
        super(descriptor, Primitive.FLOAT);
    }

    public FloatType() {
        super(Primitive.FLOAT);
    }

    @Override
    public FloatType array() {
        return new FloatType("[" + descriptor);
    }

    @Override
    public int loadCode() {
        if(isArray()) return Opcodes.ALOAD;
        return Opcodes.FLOAD;
    }

    @Override
    public int storeCode() {
        if(isArray()) return Opcodes.ASTORE;
        return Opcodes.FSTORE;
    }

    @Override
    public int returnCode() {
        if(isArray()) return Opcodes.ARETURN;
        return Opcodes.FRETURN;
    }

    @Override
    public int size() {
        return 1;
    }

}
