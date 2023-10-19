package me.pesekjak.hippo.core;

import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

/**
 * Represents a content that can be annotated.
 */
public interface Annotable {

    /**
     * @return annotations
     */
    @Unmodifiable Collection<Annotation> getAnnotations();

}
