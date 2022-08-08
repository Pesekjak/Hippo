package me.pesekjak.hippo.classes.content;

import ch.njol.skript.lang.Expression;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Enum extends Field {

    @Getter
    private final List<Type> superArguments = new ArrayList<>();
    @Getter @Setter
    private Expression<?> superValues;

    public Enum(String name, Type type) {
        super(name, type);
        addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
    }

    @Override
    public void setup(final IClassBuilder CB) {
        int modifiers = ClassBuilder.sumModifiers(getModifiers().toArray(new Modifier[0])) + Opcodes.ACC_ENUM; // Enums have extra modifier
        final FieldVisitor FV = CB.visitor().visitField(modifiers, getName(), getDescriptor(), null, null);
        getAnnotations().forEach(annotation -> annotation.setupFieldAnnotation(CB, FV)); // setups annotations
        FV.visitEnd();
        CB.visitor().visitEnd();
    }

    public String getSuperDescriptor() {
        StringBuilder argumentsDescriptor = new StringBuilder();
        for(Type argument : superArguments) {
            argumentsDescriptor.append(argument.descriptor());
        }
        return "(" + argumentsDescriptor + ")V";
    }

    public void addSuperArguments(Type... arguments) {
        superArguments.addAll(Arrays.asList(arguments));
    }

    public Object getSuperValue(int index) {
        if(--index < 0) return null;
        Object[] values = superValues.getAll(new MethodCallEvent(null, getIdentifier(), this));
        if(!(values.length > index))
            return null;
        return values[index];
    }

}
