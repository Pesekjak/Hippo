package me.pesekjak.hippo.classes.types;

import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import org.objectweb.asm.Opcodes;

public class VoidType extends PrimitiveType {

    public VoidType() {
        super(Primitive.VOID);
    }

    @Override
    public Type array() {
        throw new UnsupportedOperationException("Void type doesn't support array types.");
    }

    @Override
    public int loadCode() {
        throw new UnsupportedOperationException("Void type can't be loaded.");
    }

    @Override
    public int storeCode() {
        throw new UnsupportedOperationException("Void type can't be stored.");
    }

    @Override
    public int returnCode() {
        return Opcodes.RETURN;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Void type can't be stored.");
    }

}
