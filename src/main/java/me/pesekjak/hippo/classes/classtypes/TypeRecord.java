package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TypeRecord extends SkriptClass {

    private final List<Argument> recordConstructorArguments;

    public TypeRecord(@NotNull Type type) {
        super(type, ClassType.RECORD);
        this.getExtendingTypes().clear();
        this.addExtendingType(new Type(java.lang.Record.class));
        recordConstructorArguments = new ArrayList<>();
    }

    public List<Argument> getRecordConstructorArguments() {
        return recordConstructorArguments;
    }

    public void addRecordConstructorArgument(Argument argument) {
        recordConstructorArguments.add(argument);
    }

}
