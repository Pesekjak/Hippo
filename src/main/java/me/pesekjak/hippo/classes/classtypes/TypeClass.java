package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import org.jetbrains.annotations.NotNull;

public class TypeClass extends SkriptClass {

    public TypeClass(@NotNull Type type) {
        super(type, ClassType.CLASS);
    }

}
