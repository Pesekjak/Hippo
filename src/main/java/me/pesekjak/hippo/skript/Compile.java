package me.pesekjak.hippo.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.skript.reflect.ExprJavaCall;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.classtypes.SkriptClass;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.Reflectness;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public class Compile {

    public static class EffCompileStatus extends Effect {

        private ISkriptClass skriptClass;
        private int pattern;

        static {
            Skript.registerEffect(
                    EffCompileStatus.class,
                    "compile [[this|[the] current] [skript(-| )]class] now",
                    "don't compile [[this|[the] current] [skript(-| )]class]"
            );
        }

        @Override
        protected void execute(@NotNull Event event) {

        }

        @Override
        public @NotNull String toString(Event event, boolean b) {
            if(pattern == 0) return "early compile of class " + skriptClass.getType().dotPath();
            return "disabling auto class compiling for " + skriptClass.getType().dotPath();
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            if(!getParser().isCurrentEvent(NewSkriptClassEvent.class))
                return false;
            pattern = i;
            skriptClass = SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass();

            if(pattern == 0) {
                skriptClass.setCompileStatus(ISkriptClass.CompileStatus.PARSE);
                ClassBuilder.forClass(skriptClass).build();
            } else {
                skriptClass.setCompileStatus(ISkriptClass.CompileStatus.MANUAL);
            }

            return true;
        }

    }

    public static class EffCompile extends Effect {

        private Expression<Object> typeExpression;
        private boolean force;
        private Node node;

        static {
            Skript.registerEffect(
                    EffCompile.class,
                    "(0¦|1¦try force) (compile|define) [[the] [skript(-| )]class] %javatype%");
        }

        @Override
        protected void execute(@NotNull Event event) {
            Type type = ExprType.typeFromObject(typeExpression.getSingle(
                    new NewSkriptClassEvent(new SkriptClass(new NonPrimitiveType(Object.class))))
            );
            if(type == null)
                return;
            ISkriptClass skriptClass = SkriptClassBuilder.getSkriptClass(type.dotPath());
            if(skriptClass == null)
                return;
            AtomicReference<Throwable> throwable = new AtomicReference<>();

            ClassBuilder.forClass(skriptClass).build(force, throwable);

            if(throwable.get() != null) {
                Reflectness.setField(Reflectness.getField("lastError", ExprJavaCall.class), null, throwable.get());
                String message = null;
                Field messageField = Reflectness.getField("detailMessage", Throwable.class);
                if(messageField != null)
                    message = (String) Reflectness.getField(messageField, throwable.get());
                Logger.severe("Force compiling of class '" + type.dotPath() + "' failed: " +
                        node.toString().replaceAll("&[0-9a-f]", "") +
                        " (Cause: " + throwable.get().getClass().getName() + ", detail message: " + (message != null ? message : "Unknown") + ")");
            }
        }

        @Override
        public @NotNull String toString(Event event, boolean b) {
            Type type = ExprType.typeFromObject(typeExpression.getSingle(event));
            if(type != null)
                return "manual compiling of " + type.dotPath();
            else
                return "manual compiling";
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            typeExpression = SkriptUtil.defendExpression(expressions[0]);
            if(typeExpression == null)
                return false;
            force = parseResult.mark == 1;
            node = getParser().getNode();
            return true;
        }

    }

}
