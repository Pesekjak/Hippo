package me.pesekjak.hippo.classes.types.primitives;

import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.types.PrimitiveType;
import org.objectweb.asm.Opcodes;

public class BooleanType extends PrimitiveType {

    protected BooleanType(String descriptor) {
        super(descriptor, Primitive.BOOLEAN);
    }

    public BooleanType() {
        super(Primitive.BOOLEAN);
    }

    @Override
    public BooleanType array() {
        return new BooleanType("[" + descriptor);
    }

    @Override
    public int loadCode() {
        if(isArray()) return Opcodes.ALOAD;
        return Opcodes.ILOAD;
    }

    @Override
    public int storeCode() {
        if(isArray()) return Opcodes.ASTORE;
        return Opcodes.ISTORE;
    }

    @Override
    public int returnCode() {
        if(isArray()) return Opcodes.ARETURN;
        return Opcodes.IRETURN;
    }

    @Override
    public int size() {
        return 1;
    }

}
