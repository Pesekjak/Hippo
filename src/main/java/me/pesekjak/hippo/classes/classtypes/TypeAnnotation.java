package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import org.jetbrains.annotations.NotNull;

public class TypeAnnotation extends SkriptClass {

    public TypeAnnotation(@NotNull Type type) {
        super(type, ClassType.ANNOTATION);
    }

    @Override
    public String toJavaCode() {
        return null;
    }

}
