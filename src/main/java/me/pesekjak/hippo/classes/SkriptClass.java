package me.pesekjak.hippo.classes;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import me.pesekjak.hippo.classes.contents.Constructor;
import me.pesekjak.hippo.classes.contents.Field;
import me.pesekjak.hippo.classes.contents.Method;
import me.pesekjak.hippo.utils.events.classcontents.StaticInitializationEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class SkriptClass extends Annotatable {

    private final Type type;
    private final ClassType classType;

    private final List<Type> extendingTypes = new ArrayList<>();
    private final List<Type> implementingTypes = new ArrayList<>();

    private final HashMap<String, Field> fields = new HashMap<>();
    private final HashMap<String, Method> methods = new HashMap<>();
    private final HashMap<String, Constructor> constructors = new HashMap<>();

    private Trigger staticTrigger;

    private Event defineEvent;

    public SkriptClass(@NotNull Type type, @NotNull ClassType classType) {
        this.type = type;
        this.classType = classType;
    }

    public Type getType() {
        return type;
    }

    public ClassType getClassType() {
        return classType;
    }

    public String getClassName() {
        return type.getDotPath();
    }

    public List<Type> getExtendingTypes() {
        return extendingTypes;
    }

    public void addExtendingType(Type extending) {
        if(!extendingTypes.contains(extending)) extendingTypes.add(extending);
    }

    public void removeExtendingType(Type extending) {
        extendingTypes.remove(extending);
    }

    public List<Type> getImplementingTypes() {
        return implementingTypes;
    }

    public void addImplementingType(Type implementing) {
        if(!implementingTypes.contains(implementing)) implementingTypes.add(implementing);
    }

    public void removeImplementingType(Type implementing) {
        implementingTypes.remove(implementing);
    }

    public HashMap<String, Field> getFields() {
        return fields;
    }

    public Field getField(String name) {
        return fields.get(name);
    }

    public void addField(String name, Field field) {
        fields.putIfAbsent(name, field);
        fields.replace(name, field);
    }

    public void removeField(String name) {
        fields.remove(name);
    }

    public HashMap<String, Method> getMethods() {
        return methods;
    }

    public Method getMethod(String name) {
        return methods.get(name);
    }

    public void addMethod(String name, Method method) {
        methods.putIfAbsent(name, method);
        methods.replace(name, method);
    }

    public void removeMethod(String name) {
        methods.remove(name);
    }

    public HashMap<String, Constructor> getConstructors() {
        return constructors;
    }

    public Constructor getConstructor(String name) {
        return constructors.get(name);
    }

    public void addConstructor(String name, Constructor constructor) {
        constructors.putIfAbsent(name, constructor);
        constructors.replace(name, constructor);
    }

    public void removeConstructor(String name) {
        constructors.remove(name);
    }

    public void setStaticTrigger(Trigger staticTrigger) {
        this.staticTrigger = staticTrigger;
    }

    public Trigger getStaticTrigger() {
        return staticTrigger;
    }

    public void runStaticInitialization() {
        if(staticTrigger == null) return;
        TriggerItem.walk(staticTrigger, new StaticInitializationEvent());
    }

    public void setDefineEvent(Event defineEvent) {
        this.defineEvent = defineEvent;
    }

    public Event getDefineEvent() {
        return defineEvent;
    }

}
