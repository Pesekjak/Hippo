package me.pesekjak.hippo.core;

import org.junit.jupiter.api.Test;

import static org.objectweb.asm.Opcodes.*;

public class ModifiableTest {

    public record TestModifiable(int modifier) implements Modifiable {
        @Override
        public int getModifier() {
            return modifier;
        }
        @Override
        public int getCompatibleModifiers() {
            return ACC_PUBLIC | ACC_PRIVATE | ACC_STATIC;
        }
    }

    @Test
    public void compatibleTest() throws IllegalModifiersException {
        Modifiable.checkModifiers(new TestModifiable(ACC_PUBLIC | ACC_STATIC), "test");
    }

    @Test
    public void incompatibleTest() throws IllegalModifiersException {
        try {
            Modifiable.checkModifiers(new TestModifiable(ACC_PUBLIC | ACC_ABSTRACT), "test");
            throw new Error();
        } catch (IllegalModifiersException ignored) { }
    }

    @Test
    public void multipleAccessTest() throws IllegalModifiersException {
        try {
            Modifiable.checkModifiers(new TestModifiable(ACC_PUBLIC | ACC_PRIVATE), "test");
            throw new Error();
        } catch (IllegalModifiersException ignored) { }
    }

}
