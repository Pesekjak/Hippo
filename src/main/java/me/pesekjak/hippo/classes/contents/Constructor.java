package me.pesekjak.hippo.classes.contents;

import me.pesekjak.hippo.classes.Argument;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.PrimitiveType;

import java.util.ArrayList;
import java.util.List;

public class Constructor extends Method {

    private final List<Argument> superArguments;

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

    public static Constructor getDefault() {
        Constructor defaultConstructor = new Constructor();
        defaultConstructor.addModifier(Modifier.PUBLIC);
        return defaultConstructor;
    }

}
