package me.pesekjak.hippo.core.skript;

import ch.njol.skript.lang.Trigger;
import me.pesekjak.hippo.core.ClassContent;
import me.pesekjak.hippo.core.StaticBlock;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wrapper for a static block.
 * <p>
 * Serves as a bridge between user Skript code and compiled class.
 */
public final class StaticBlockWrapper implements ClassContentSkriptWrapper {

    private final StaticBlock staticBlock;
    private final List<Trigger> staticTriggers;

    public StaticBlockWrapper(StaticBlock staticBlock, Trigger... triggers) {
        this.staticBlock = staticBlock;
        staticTriggers = new ArrayList<>(List.of(triggers));
    }

    /**
     * @return wrapped static block
     */
    public StaticBlock staticBlock() {
        return staticBlock;
    }

    /**
     * @return static block Skript triggers
     */
    public @Unmodifiable List<Trigger> staticTriggers() {
        return Collections.unmodifiableList(staticTriggers);
    }

    /**
     * Adds new Skript trigger to the triggers of this static block.
     *
     * @param trigger trigger to add
     */
    public void addStaticTrigger(Trigger trigger) {
        staticTriggers.add(trigger);
    }

    @Override
    public ClassContent content() {
        return staticBlock;
    }

    /**
     * Injects code to the method writer of the static block.
     */
    public void injectCode() {
        assert staticBlock.getSource() != null;
        String className = staticBlock.getSource().getName();

        staticBlock.setWriter((method, methodVisitor) -> {
            methodVisitor.visitLdcInsn(className);
            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getType(Storage.class).getInternalName(),
                    "runStaticBlock",
                    Type.getMethodDescriptor(
                            Type.VOID_TYPE,
                            Type.getType(String.class)
                    ),
                    false
            );
        });
    }

}
