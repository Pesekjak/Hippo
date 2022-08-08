package me.pesekjak.hippo.classes;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.IClassBuilder;
import me.pesekjak.hippo.classes.content.ClassContent;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.classes.content.Field;
import me.pesekjak.hippo.classes.converters.NumberConverter;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.utils.events.StaticEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

public abstract class ISkriptClass extends Modifiable {

    @Getter
    private final Type type;
    @Getter @Setter
    private Type superClass = new NonPrimitiveType(Object.class);
    @Getter
    private final List<Type> interfaces = new ArrayList<>();
    @Getter
    private final Map<String, ClassContent> classContent = new LinkedHashMap<>();
    @Getter @Setter @NotNull
    private CompileStatus compileStatus = CompileStatus.AFTER_PARSING;
    @Getter @Setter @Nullable
    private Trigger staticTrigger;

    public ISkriptClass(Type type) {
        this.type = type;
    }

    /**
     * Returns ClassType of the class.
     * @return ClassType of the class
     */
    public abstract ClassType getClassType();

    /**
     * Checks if combination of modifiers is illegal for the class content.
     * @param contentType Content to check for
     * @param modifiers Modifiers to check for
     * @return true if combination is illegal
     */
    public abstract boolean illegalModifiers(ContentType contentType, List<Modifier> modifiers);

    /**
     * Checks if class can have certain type of content added.
     * @param contentType Content type to check
     * @return false if class can't have the content of given type
     */
    public abstract boolean canHave(ContentType contentType);

    /**
     * Checks if class of the type can extend another one.
     * @return true if class can extend another one
     */
    public abstract boolean canExtend();

    /**
     * Setups the class for the ClassBuilder.
     * @apiNote Modifiers, annotations etc. should be done in {@link IClassBuilder#build()}
     * @param CB ClassBuilder building the class
     * @return true if class setup was successful
     */
    public boolean setup(final IClassBuilder CB) {

        getAnnotations().forEach(annotation -> annotation.setupClassAnnotation(CB)); // setups annotations

        // Setups the static block
        MethodVisitor MV = CB.visitor().visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        MV.visitCode();

        // Setups the static pre-set fields
        for(ClassContent content : classContent.values()) {
            if(!(content instanceof Field field)) continue;
            if(!field.getModifiers().contains(Modifier.STATIC)) continue; // Non static fields are handled in constructors

            CB.pushClass(MV);
            ClassBuilder.pushValue(MV, field.getIdentifier());
            MV.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    new NonPrimitiveType(ISkriptClass.class).internalName(),
                    "getClassContent",
                    "(Ljava/lang/String;)Lme/pesekjak/hippo/classes/content/ClassContent;",
                    false);
            ClassBuilder.cast(MV, new NonPrimitiveType(Field.class));
            MV.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    new NonPrimitiveType(Field.class).internalName(),
                    "getActualValue", "()Ljava/lang/Object;",
                    false);

            ClassBuilder.reflectConverter(MV);
            if(NumberConverter.isNumber(field.getType())) // Is Number and needs to be converted to right value.
                ClassBuilder.numberConverter(MV, field.getType());
            if(field.getType().storeCode() != Opcodes.ASTORE) { // Stores as Primitive, needs to be converted.
                if(NumberConverter.isNumber(field.getType())) // Converts using Number Converter.
                    ClassBuilder.primitiveNumberConverter(MV, field.getType());
                else // Converts using Primitive Converter.
                    ClassBuilder.primitiveConverter(MV, field.getType());
            } else {
                ClassBuilder.cast(MV, field.getType());
            }
            ClassBuilder.putField(MV, field, CB.skriptClass().getType());
        }

        // Triggers the static section
        CB.pushClass(MV);
        MV.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                new NonPrimitiveType(ISkriptClass.class).internalName(),
                "triggerStatic", "()V",
                false);

        MV.visitInsn(Opcodes.RETURN);
        MV.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        MV.visitEnd();
        CB.visitor().visitEnd();

        return true;
    }

    public void addInterfaces(Type... interfaces) {
        this.interfaces.addAll(Arrays.asList(interfaces));
    }

    public ClassContent getClassContent(String identifier) {
        return classContent.get(identifier);
    }

    public void addContent(ClassContent... contents) {
        for(ClassContent content : contents) {
            if(containsContent(content)) continue;
            classContent.put(content.getIdentifier(), content);
        }
    }

    public boolean containsContent(ClassContent content) {
        return classContent.containsKey(content.getIdentifier());
    }

    public void triggerStatic() {
        if(staticTrigger != null)
            TriggerItem.walk(staticTrigger, new StaticEvent());
    }

    public enum CompileStatus {

        PARSE,
        AFTER_PARSING,
        MANUAL

    }

}
