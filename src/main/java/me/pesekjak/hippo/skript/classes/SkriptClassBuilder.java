package me.pesekjak.hippo.skript.classes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.content.Annotation;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;

import java.util.*;

/**
 * Tracks class that is currently being built, annotations
 * and classes registered by user.
 */
@RequiredArgsConstructor
public class SkriptClassBuilder {

    public static SkriptClassBuilder ACTIVE_BUILDER;
    public static final Map<String, ISkriptClass> CLASS_REGISTRY = new HashMap<>();

    @Getter
    private final ISkriptClass skriptClass;
    @Getter @Setter
    private NewSkriptClassEvent event;
    @Getter
    private final List<Annotation> stackedAnnotations = new ArrayList<>();

    public static ISkriptClass create(ISkriptClass skriptClass) {
        ACTIVE_BUILDER = new SkriptClassBuilder(skriptClass);
        CLASS_REGISTRY.put(skriptClass.getType().dotPath(), skriptClass);
        return skriptClass;
    }

    public static ISkriptClass getSkriptClass(String name) {
        return CLASS_REGISTRY.get(name);
    }

    public void addStackedAnnotations(Annotation... annotations) {
        stackedAnnotations.addAll(Arrays.asList(annotations));
    }

    public void clearStackedAnnotations() {
        stackedAnnotations.clear();
    }

}
