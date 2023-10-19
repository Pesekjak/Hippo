package me.pesekjak.hippo.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.skript.reflect.ExprJavaCall;
import me.pesekjak.hippo.bukkit.*;
import me.pesekjak.hippo.core.AbstractClass;
import me.pesekjak.hippo.core.loader.DynamicClassLoader;
import me.pesekjak.hippo.elements.SyntaxCommons;
import me.pesekjak.hippo.utils.MethodDescriptor;
import me.pesekjak.hippo.utils.ReflectionUtil;
import me.pesekjak.hippo.utils.SkriptUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;

@Name("Result of Superclass Method Call")
@Description("Calls method of the parent class.")
@Examples({
        "public class Elephant extends Animal:",
        "\t@Override",
        "\tpublic int value():",
        "\t\tset {_v} to super.value()",
        "\t\treturn {_v} + 1"
})
@Since("1.1")
public class ExprSuperMethodCall extends SimpleExpression<Object> {

    public static final String[] PATTERNS = new String[] {
            "(0¦|1¦try) super.<" + SyntaxCommons.VARIABLE_NAME + ">[\\[[<.+>]\\]]\\([%-objects%]\\)",
            "(0¦|1¦try) super..%string%[\\[[<.+>]\\]]\\([%-objects%]\\)"
    };

    private static final Field LAST_EXCEPTION_FIELD;

    static {
        try {
            LAST_EXCEPTION_FIELD = ExprJavaCall.class.getDeclaredField("lastError");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Function<Event, String> methodName;
    private Expression<?> arguments;
    private @Nullable MethodDescriptor descriptor;
    private boolean catchException;

    private File script;
    private Node node;

    static {
        Skript.registerExpression(
                ExprSuperMethodCall.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
                PATTERNS
        );
    }

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        if (!(event instanceof InstanceEvent instanceEvent)) return new Object[0];

        Class<?> superClazz;

        try {
            DynamicClassLoader classLoader = DynamicClassLoader.getInstance();
            AbstractClass source = instanceEvent.getSource();
            superClazz = classLoader.loadClass(source.getSuperClass().getClassName());
        } catch (Throwable throwable) {
            return new Object[0];
        }

        String methodName = this.methodName.apply(event);
        Object[] arguments = this.arguments != null ? this.arguments.getAll(event) : new Object[0];

        Method found;

        if (descriptor != null) {
            try {
                found = descriptor.get(script, methodName, superClazz);
            } catch (Exception exception) {
                SkriptUtil.warning(node, "There is no method with descriptor '" + descriptor.stringDescriptor() + "'");
                return new Object[0];
            }
        } else {
            found = ReflectionUtil.searchMethodFromArguments(superClazz, methodName, arguments);
            if (found == null) {
                SkriptUtil.warning(node,
                        "No matching super method: " + superClazz.getSimpleName() + "#" + methodName
                                + " called "
                                + (arguments.length == 0
                                ? "without arguments"
                                : "with " + Arrays.toString(arguments)
                                )
                );
                return new Object[0];
            }
        }

        if (Modifier.isStatic(found.getModifiers())) {
            SkriptUtil.warning(node, "You can not invoke static methods using super keyword");
            return new Object[0];
        }

        MethodHandle handle;
        try {
            handle = ReflectionUtil.unreflectSpecial(superClazz, found, superClazz, false);
        } catch (Throwable throwable) {
            SkriptUtil.warning(node, "Failed to access method: '" + found + "'");
            return new Object[0];
        }

        Object result;
        try {
            result = ReflectionUtil.convertArgsAndInvokeMethod(handle, instanceEvent.getInstance(), arguments);
        } catch (Throwable throwable) {
            if (throwable instanceof ClassCastException || throwable instanceof WrongMethodTypeException)
                SkriptUtil.warning(node, "Failed to execute method '" + methodName + "' with provided arguments");
            else if (!catchException) {
                String message = "Method '" + methodName + "' threw a " + throwable.getClass().getSimpleName();
                if (throwable.getMessage() != null)
                    message = message + " (" + throwable.getMessage() + ")";
                SkriptUtil.warning(node, message);
            } else {
                try {
                    LAST_EXCEPTION_FIELD.set(null, throwable);
                } catch (IllegalAccessException ignored) { }
            }

            return new Object[0];
        }

        return new Object[] {result};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "super method call";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(MethodCallEvent.class, ConstructorCallEvent.class)) return false;
        if (!(getParser().getCurrentStructure() instanceof SectionSkriptEvent)) return false;

        int descriptorRegexIndex;

        if (matchedPattern == 0) {
            String methodName = parseResult.regexes.get(0).group();
            this.methodName = event -> methodName;
            arguments = LiteralUtils.defendExpression(expressions[0]);

            descriptorRegexIndex = 1;
        } else {
            Expression<?> defended = LiteralUtils.defendExpression(expressions[0]);
            this.methodName = event -> (String) defended.getSingle(event);
            arguments = LiteralUtils.defendExpression(expressions[1]);

            descriptorRegexIndex = 0;
        }

        if (parseResult.regexes.size() > descriptorRegexIndex) {
            descriptor = new MethodDescriptor(parseResult.regexes.get(descriptorRegexIndex).group());
        }

        catchException = parseResult.mark == 1;

        script = SkriptUtil.getCurrentScript(getParser());
        node = getParser().getNode();

        return true;
    }

    @Override
    public Class<?> @NotNull [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return new Class<?>[0];
    }

}
