package me.pesekjak.hippo.classes.content;

import ch.njol.skript.lang.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.pesekjak.hippo.classes.Modifiable;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.classtypes.SkriptClass;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.FieldVisitor;

@RequiredArgsConstructor
public class Field extends Modifiable implements ClassContent {

    protected final String name;
    protected final Type type;

    @Getter @Setter @Nullable
    private Expression<?> value = null;

    public Field(String name, Type type, @Nullable Expression<?> value) {
        this(name, type);
        this.value = value;
    }

    @Override
    public String getIdentifier() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getDescriptor() {
        return type.descriptor();
    }

    @Override
    public void setup(final IClassBuilder CB) {
        int modifiers = ClassBuilder.sumModifiers(getModifiers().toArray(new Modifier[0]));
        final FieldVisitor FV = CB.visitor().visitField(modifiers, getName(), getDescriptor(), null, null);
        getAnnotations().forEach(annotation -> annotation.setupFieldAnnotation(CB, FV)); // setups annotations
        FV.visitEnd();
        CB.visitor().visitEnd();
    }

    public Object getActualValue() {
        if(value == null) return null;
        return value.getSingle(new NewSkriptClassEvent(
                        new SkriptClass(new NonPrimitiveType(Object.class))
        ));
    }

}
