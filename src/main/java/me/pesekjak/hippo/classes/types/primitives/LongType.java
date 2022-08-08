package me.pesekjak.hippo.classes.types.primitives;

import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.types.PrimitiveType;
import org.objectweb.asm.Opcodes;

public class LongType extends PrimitiveType {

    protected LongType(String descriptor) {
        super(descriptor, Primitive.LONG);
    }

    public LongType() {
        super(Primitive.LONG);
    }

    @Override
    public LongType array() {
        return new LongType("[" + descriptor);
    }

    @Override
    public int loadCode() {
        if(isArray()) return Opcodes.ALOAD;
        return Opcodes.LLOAD;
    }

    @Override
    public int storeCode() {
        if(isArray()) return Opcodes.ASTORE;
        return Opcodes.LSTORE;
    }

    @Override
    public int returnCode() {
        if(isArray()) return Opcodes.ARETURN;
        return Opcodes.LRETURN;
    }

    @Override
    public int size() {
        return isArray() ? 1 : 2;
    }

}
