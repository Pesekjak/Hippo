package me.pesekjak.hippo.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a static block of a class.
 */
public final class StaticBlock extends Method {

    public StaticBlock(AbstractClass source) throws IllegalModifiersException {
        super(
                source,
                Constants.STATIC_BLOCK_METHOD_NAME,
                new Parameter(Type.VOID_TYPE, Collections.emptyList()),
                Collections.emptyList(),
                Opcodes.ACC_STATIC,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    @Override
    public void visit(ClassVisitor visitor) {
        // Static block has no exceptions
        MethodVisitor methodVisitor = visitor.visitMethod(getModifier(), getName(), getDescriptor(), null, new String[0]);

        methodVisitor.visitCode();

        // Visiting source class
        assert getSource() != null;
        getSource().visitClinit(this, methodVisitor);

        // Visiting content
        Collection<ClassContent> contents = getSource().getContents();
        contents.forEach(content -> {
            if (content instanceof AbstractClass innerClass)
                innerClass.visitOuterClinit(this, methodVisitor);
            else
                content.visitClinit(this, methodVisitor);
        });


        // Accepting the instructions writer
        if (getWriter() != null) getWriter().accept(this, methodVisitor);

        methodVisitor.visitInsn(Opcodes.RETURN); // Constructors should always have void type
        methodVisitor.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        methodVisitor.visitEnd();
        visitor.visitEnd();
    }

}
