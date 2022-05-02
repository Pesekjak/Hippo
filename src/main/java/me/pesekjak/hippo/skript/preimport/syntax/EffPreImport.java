package me.pesekjak.hippo.skript.preimport.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.preimport.PreImport;
import me.pesekjak.hippo.preimport.PreImportManager;
import me.pesekjak.hippo.utils.events.PreImportEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EffPreImport extends Effect {

    static {
        Skript.registerEffect(EffPreImport.class,
                "<([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*> [as <[a-zA-Z0-9]*>]"
        );
    }

    private Type type;
    private PreImport preImport;
    private String classAlias;
    private Config script;

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "preimported" + classAlias;
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(PreImportEvent.class)) return false;

        script = getParser().getCurrentScript();
        type = new Type(parseResult.regexes.get(0).group());
        classAlias = type.getSimpleName();
        preImport = new PreImport(type);

        if(parseResult.regexes.size() > 1) {
            classAlias = parseResult.regexes.get(1).group();
        }

        PreImportManager.PreImporting preImporting = PreImportManager.MANAGER.getPreImporting(script);

        if(preImporting.containsAlias(classAlias)) {
            Skript.error("Alias '" + classAlias + "' is already mapped to different pre-import");
            return false;
        }

        if(preImporting.containsPreImport(preImport)) {
            Skript.error("Class '" + preImport.preImportType().getDotPath() + "' is already pre-imported");
            return false;
        }

        preImporting.addPreImport(classAlias, preImport);

        return true;
    }
}
