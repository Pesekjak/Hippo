package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import org.jetbrains.annotations.NotNull;

public class TypeEnum extends SkriptClass {

    public TypeEnum(@NotNull Type type) {
        super(type, ClassType.ENUM);
    }

    @Override
    public String toJavaCode() {
        return null;
    }

}
