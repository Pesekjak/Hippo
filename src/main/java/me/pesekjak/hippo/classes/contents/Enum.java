package me.pesekjak.hippo.classes.contents;

import me.pesekjak.hippo.classes.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Enum extends Field {

    private final List<Argument> superArguments;

    public Enum(@NotNull Type type, @NotNull String name) {
        super(new PrimitiveType(Primitive.NONE), type, name);
        this.addModifier(Modifier.PUBLIC);
        this.addModifier(Modifier.STATIC);
        this.addModifier(Modifier.FINAL);
        superArguments = new ArrayList<>();
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
        return "(Ljava/lang/String;I" + argumentsDescriptor + ")V";
    }

}
