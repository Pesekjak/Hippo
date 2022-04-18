package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.lang.Expression;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for easier ClassBuilding out of Skript's expressions
 */
public class SkriptClassBuilder {

    public static SkriptClass registeringClass;
    public static Event registeringEvent;
    public static List<Annotation> stackedAnnotations = new ArrayList<>();

    private SkriptClassBuilder() { }

    /**
     * @return Class that's currently being registered (code of the class is being run by Skript)
     */
    public static SkriptClass getRegisteringClass() {
        return registeringClass;
    }

    public static void setRegisteringClass(SkriptClass registeringClass) {
        SkriptClassBuilder.registeringClass = registeringClass;
    }

    /**
     * @return Event of currently registering Skript Class
     */

    public static Event getCurrentEvent() {
        return registeringClass.getDefineEvent();
    }

    /**
     * Returns Hippo's Type from expressions with return type of Reflect's JavaType
     * @param typeExpression Expression of Reflect's JavaType
     * @return Hippo Type out of typeExpression
     */
    public static Type getTypeFromExpression(Expression<?> typeExpression) {
        Type type = null;
        if(typeExpression != null) {
            Object typeObject = typeExpression.getSingle(SkriptClassBuilder.getCurrentEvent());
            if(typeObject == null) return null;
            if(typeObject instanceof Type) {
                // In case class doesn't exist yet and expression returned Hippo's type,
                // happens with PreImported classes
                type = (Type) typeObject;
            } else {
                try {
                    // In case provided expression is Reflect's JavaType expression or
                    // different source of JavaType
                    type = new Type(SkriptReflectHook.classOfJavaType(typeObject));
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return type;
    }

}
