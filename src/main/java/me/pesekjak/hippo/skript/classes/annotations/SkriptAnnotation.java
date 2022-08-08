package me.pesekjak.hippo.skript.classes.annotations;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.util.SkriptUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.content.Annotation;
import me.pesekjak.hippo.skript.ExprType;
import me.pesekjak.hippo.skript.classes.Buildable;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.pesekjak.hippo.skript.Pair.KEY_PATTERN;

@RequiredArgsConstructor
public class SkriptAnnotation {

    static {
        Skript.registerEffect(EffStackedAnnotations.class,
                "%annotations%"
        );
        Skript.registerExpression(
                ExprAnnotation.class, Annotation.class, ExpressionType.COMBINED,
                "\\@%-javatype%[\\([%-annotationelements%]\\)]"
        );
        Skript.registerExpression(
                ExprAnnotationElement.class, AnnotationElement.class, ExpressionType.COMBINED,
                "<" + KEY_PATTERN + "> = %-boolean/string/character/annotationvalue%",
                "<" + KEY_PATTERN + "> = \\[%-booleans/strings/characters/annotationvalues%\\]");
    }

    @RequiredArgsConstructor
    public static class AnnotationElement {

        @Getter
        protected final String name;
        @Getter
        protected final List<Object> objects = new ArrayList<>();
        @Setter
        protected boolean isArray = false;

        public Object getSingle() {
            return objects.get(0);
        }

        public void addObjects(Object... objects) {
            this.objects.addAll(Arrays.asList(objects));
        }

        public boolean isArray() {
            if(objects.size() > 1) return true;
            return isArray;
        }

    }

    public static class EffStackedAnnotations extends Effect implements Buildable {

        @Getter // For EvtNewSkriptClass when getting class annotations
        private Expression<Annotation> annotationExpression;

        @Override
        protected void execute(@NotNull Event event) {

        }

        @Override
        public @NotNull String toString(Event event, boolean b) {
            return "stacked annotation";
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
            annotationExpression = SkriptUtil.defendExpression(expressions[0]);
            return build(SkriptClassBuilder.ACTIVE_BUILDER.getEvent());
        }

        @Override
        public boolean build(Event event) {
            SkriptClassBuilder.ACTIVE_BUILDER.addStackedAnnotations(annotationExpression.getAll(event));
            return true;
        }

    }

    public static class ExprAnnotation extends SimpleExpression<Annotation> {

        private Expression<Object> typeExpression;
        private Expression<AnnotationElement> elementExpression;

        @Override
        protected Annotation @NotNull [] get(@NotNull Event event) {
            Type type = ExprType.typeFromObject(typeExpression.getSingle(event));
            if(type == null) return new Annotation[0];
            Annotation annotation = new Annotation(type);
            if(elementExpression != null) {
                Set<String> names = new HashSet<>();
                for(AnnotationElement element : elementExpression.getAll(event)) {
                    if(names.contains(element.name)) {
                        Skript.error("Name '" + element.name + "' is already assigned to different element");
                        continue;
                    }
                    names.add(element.name);
                    annotation.addElements(element);
                }
            }
            return new Annotation[] {annotation};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<? extends Annotation> getReturnType() {
            return Annotation.class;
        }

        @Override
        public @NotNull String toString(Event event, boolean b) {
            Type type = ExprType.typeFromObject(typeExpression.getSingle(event));
            if(type == null) return "annotation";
            return "annotation @" + type.dotPath();
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
            typeExpression = SkriptUtil.defendExpression(expressions[0]);
            elementExpression = SkriptUtil.defendExpression(expressions[1]);
            return true;
        }

    }

    public static class ExprAnnotationElement extends SimpleExpression<AnnotationElement> {

        private String name;
        private Expression<?> valueExpression;

        private int pattern;

        @Override
        protected AnnotationElement @NotNull [] get(@NotNull Event event) {
            Object[] values = valueExpression.getAll(event);
            if(values.length == 0) return new AnnotationElement[0];
            AnnotationElement element = new AnnotationElement(name);
            for(Object o : values) {
                if(o instanceof DefaultValue defaultValue)
                    element.addObjects(defaultValue.getValue());
                else
                    element.addObjects(o);
            }
            if(pattern == 1) element.setArray(true);
            return new AnnotationElement[] {element};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<? extends AnnotationElement> getReturnType() {
            return AnnotationElement.class;
        }

        @Override
        public @NotNull String toString(Event event, boolean b) {
            return "annotation element " + name;
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
            name = parseResult.regexes.get(0).group();
            valueExpression = SkriptUtil.defendExpression(expressions[0]);
            pattern = i;
            return true;
        }

    }

}
