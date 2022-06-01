package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.contents.Enum;
import me.pesekjak.hippo.classes.contents.Field;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TypeEnum extends SkriptClass {

    public TypeEnum(@NotNull Type type) {
        super(type, ClassType.ENUM);
        this.getExtendingTypes().clear();
        this.addExtendingType(new Type(java.lang.Enum.class));
    }

    public List<Enum> getEnumFields() {
        List<Enum> enums = new ArrayList<>();
        for(Field field : getFields().values()) {
            if(field instanceof Enum) enums.add((Enum) field);
        }
        return enums;
    }

}
