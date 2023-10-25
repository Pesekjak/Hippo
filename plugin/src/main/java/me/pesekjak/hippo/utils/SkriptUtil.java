package me.pesekjak.hippo.utils;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.JavaType;
import me.pesekjak.hippo.core.PreImport;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.*;
import java.util.function.Consumer;

/**
 * Utils related to Skript.
 */
public final class SkriptUtil {

    private SkriptUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns file for current script of given parser.
     *
     * @param parser parser instance
     * @return file
     */
    public static @Nullable Script getCurrentScript(ParserInstance parser) {
        return parser.isActive() ? parser.getCurrentScript() : null;
    }

    /**
     * Sets variable to a new value.
     *
     * @param variable variable to change
     * @param event event to change the variable with
     * @param value new value
     */
    public static void setVariable(Variable<?> variable, Event event, Object value) {
        Variables.setVariable(variable.getName().toString(event), value, event, variable.isLocal());
    }

    /**
     * Loads code of a structure and returns it as unlocked trigger, {@link UnlockedTrigger}.
     *
     * @param parser parser to load the code with
     * @param structure structure with the code
     * @param name name of the structure
     * @param afterLoading after loading listener
     * @param skriptEvent skript event to load the code with
     * @param sectionNode section node of the structure
     * @param events events to load the code with
     * @return loaded unlocked trigger
     */
    @SafeVarargs
    public static UnlockedTrigger loadCode(ParserInstance parser,
                                   Structure structure,
                                   String name,
                                   @Nullable Runnable afterLoading,
                                   SkriptEvent skriptEvent,
                                   SectionNode sectionNode,
                                   Class<? extends Event>... events) {
        // replicates Section#loadCode method
        String previousName = parser.getCurrentEventName();
        Class<? extends Event>[] previousEvents = parser.getCurrentEvents();
        Structure previousStructure = parser.getCurrentStructure();
        List<TriggerSection> previousSections = parser.getCurrentSections();
        Kleenean previousDelay = parser.getHasDelayBefore();

        parser.setCurrentEvent(name, events);
        parser.setCurrentStructure(structure);
        parser.setCurrentSections(new ArrayList<>());
        parser.setHasDelayBefore(Kleenean.FALSE);
        List<TriggerItem> triggerItems = ScriptLoader.loadItems(sectionNode);
        if (afterLoading != null)
            afterLoading.run();

        //noinspection ConstantConditions - We are resetting it to what it was
        parser.setCurrentEvent(previousName, previousEvents);
        parser.setCurrentStructure(previousStructure);
        parser.setCurrentSections(previousSections);
        parser.setHasDelayBefore(previousDelay);
        return new UnlockedTrigger(parser.getCurrentScript(), name, skriptEvent, triggerItems);
    }

    /**
     * Util method used for collecting both Hippo types and reflect java types and
     * converting them to a collection of ASM types.
     *
     * @param expression expression to get the types from
     * @param event event to use
     * @return list of types
     */
    public static List<Type> collectTypes(@Nullable Expression<?> expression, Event event) {
        if (expression == null) return Collections.emptyList();
        return Arrays.stream(LiteralUtils.defendExpression(expression).getAll(event))
                .map(type -> {
                    if (type instanceof PreImport preImport) return preImport.type();
                    if (type instanceof JavaType javaType) return Type.getType(javaType.getJavaClass());
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Converts node to a human-readable string.
     *
     * @param node node to convert
     * @param config script (source)
     * @return node as string
     */
    public static String getNodeString(Node node, Config config) {
        if (node.getParent() != null) return node.toString();
        String save = node.save().trim();
        while (save.startsWith("\t"))
            save = save.replaceFirst("\t", "");
        return save + " (" + config.getFileName() + ", " + (node.getLine() == -1 ? "unknown line" : "line " + node.getLine()) + ")";
    }

    /**
     * Sends info message related to a node using Skript's logger.
     *
     * @param node node to use
     * @param message message to log
     */
    public static void info(Node node, String message) {
        log(node, message, Skript::info);
    }

    /**
     * Sends warning message related to a node using Skript's logger.
     *
     * @param node node to use
     * @param message message to log
     */
    public static void warning(Node node, String message) {
        log(node, message, Skript::warning);
    }

    /**
     * Sends error message related to a node using Skript's logger.
     *
     * @param node node to use
     * @param message message to log
     */
    public static void error(Node node, String message) {
        log(node, message, Skript::error);
    }

    /**
     * Sends message related to a node using Skript's logger.
     *
     * @param node node to use
     * @param message message to log
     * @param consumer consumer for logging the message
     */
    private static void log(Node node, String message, Consumer<String> consumer) {
        Node previous = SkriptLogger.getNode();
        SkriptLogger.setNode(node);
        consumer.accept(message);
        SkriptLogger.setNode(previous);
    }

}
