package me.pesekjak.hippo.core.skript;

import ch.njol.skript.config.SectionNode;
import me.pesekjak.hippo.core.AbstractClass;
import me.pesekjak.hippo.core.ClassContent;

/**
 * Wrapper for a class.
 * <p>
 * Serves as a bridge between user Skript code and compiled class.
 */
public final class ClassWrapper implements ClassContentSkriptWrapper {

    private final AbstractClass clazz;
    private final SectionNode node;

    public ClassWrapper(AbstractClass clazz, SectionNode node) {
        this.clazz = clazz;
        this.node = node;
    }

    /**
     * @return wrapped class
     */
    public AbstractClass getWrappedClass() {
        return clazz;
    }

    /**
     * @return section node of the class
     */
    public SectionNode getNode() {
        return node;
    }

    @Override
    public ClassContent content() {
        return clazz;
    }

}
