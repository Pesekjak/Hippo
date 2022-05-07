package me.pesekjak.hippo.classes.contents;

import ch.njol.skript.lang.Trigger;
import me.pesekjak.hippo.classes.*;

import java.util.ArrayList;
import java.util.List;

public class Constructor extends Method {

    private final List<Argument> superArguments;
    private Trigger postInitTrigger;

    public Constructor() {
        super(new PrimitiveType(Primitive.VOID), null, "<init>");
        superArguments = new ArrayList<>();
        this.setRunnable(true);
    }

    public List<Argument> getSuperArguments() {
        return superArguments;
    }

    public void addSuperArgument(Argument argument) {
        superArguments.add(argument);
    }

    public void removeSuperArgument(Argument argument) {
        superArguments.remove(argument);
    }

    public String getSuperDescriptor() { ;
        StringBuilder argumentsDescriptor = new StringBuilder();
        for(Argument argument : superArguments) {
            argumentsDescriptor.append(argument.getType() != null ? argument.getType().getDescriptor() : argument.getPrimitiveType().getDescriptor());
        }
        return "(" + argumentsDescriptor + ")V";
    }

    public Trigger getPostInitTrigger() {
        return postInitTrigger;
    }

    public void setPostInitTrigger(Trigger trigger) {
        this.postInitTrigger = trigger;
    }

    public static Constructor getDefault() {
        Constructor defaultConstructor = new Constructor();
        defaultConstructor.addModifier(Modifier.PUBLIC);
        return defaultConstructor;
    }

    public static Constructor getDefaultEnumConstructor() {
        Constructor enumConstructor = new Constructor();
        enumConstructor.addArgument(new Argument(new Type(String.class), "E1"));
        enumConstructor.addArgument(new Argument(new PrimitiveType(Primitive.INT), "E2"));
        enumConstructor.addSuperArgument(new Argument(new Type(String.class), "E1"));
        enumConstructor.addSuperArgument(new Argument(new PrimitiveType(Primitive.INT), "E2"));
        enumConstructor.addModifier(Modifier.PRIVATE);
        return enumConstructor;
    }

}
