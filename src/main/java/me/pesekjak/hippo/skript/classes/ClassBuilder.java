package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.config.Config;
import ch.njol.skript.lang.Expression;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.classes.registry.SkriptClassRegistry;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.preimport.PreImportManager;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

public class ClassBuilder {

    public static SkriptClass registeringClass;
    public static List<Annotation> stackedAnnotations = new ArrayList<>();

    private ClassBuilder() { }

    public static SkriptClass getRegisteringClass() {
        return registeringClass;
    }

    public static void setRegisteringClass(SkriptClass registeringClass) {
        ClassBuilder.registeringClass = registeringClass;
    }

    public static List<Annotation> getStackedAnnotations() {
        return stackedAnnotations;
    }

    public static void addStackingAnnotation(Annotation stackedAnnotation) {
        stackedAnnotations.add(stackedAnnotation);
    }

    public static void clearStackedAnnotations() {
        stackedAnnotations.clear();
    }

    public static boolean validate(Event event) {
        if(!(event instanceof NewSkriptClassEvent)) return false;
        if((SkriptClassRegistry.REGISTRY.getSkriptClass(((NewSkriptClassEvent) event).getClassName())) != null) return true;
        Logger.severe("You can't set properties of class '" + ((NewSkriptClassEvent) event).getClassName() + "' because type of the class wasn't assigned: " + ((NewSkriptClassEvent) event).getCurrentNode().toString());
        ((NewSkriptClassEvent) event).getCurrentTriggerItem().setNext(null);
        return false;
    }

    public static String getClassNameFromExpression(Expression<?> expression, Event event) {
        expression = SkriptUtils.defendExpression(expression);
        Object javaType = expression.getSingle(event);
        if((expression.toString().charAt(0) == '$')) {
            String classAlias = expression.toString().substring(1);
            Config config = ((NewSkriptClassEvent) event).getCurrentNode().getConfig();
            return PreImportManager.MANAGER.getPreImporting(config).getPreImport(classAlias).preImportType().getDotPath();
        }
        if(javaType != null) return SkriptReflectHook.classOfJavaType(javaType).getName();
        Logger.severe("Expression '" + expression + "' isn't supported as Class object by Hippo");
        return null;
    }

}
