package me.pesekjak.hippo.skript.classes.annotations;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import com.btk5h.skriptmirror.util.SkriptMirrorUtil;
import com.btk5h.skriptmirror.util.SkriptReflection;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreScriptLoadListener implements Listener {

    @Getter
    private static final Map<Config, Map<String, List<Node>>> CLASS_ANNOTATION_MAP = new HashMap<>();

    private static final String PATTERN = "%-modifiers% [skript(-| )]%classtype% <" + SkriptMirrorUtil.PACKAGE + "> [extends [<.+>]] [implements [<.+>]]";

    @EventHandler
    public void onPreScriptLoad(PreScriptLoadEvent event) {
        ParserInstance parser = ParserInstance.get();
        final List<Config> scripts = event.getScripts();

        for(Config script : scripts) {
            parser.setCurrentScript(script);

            List<Node> toRemove = new ArrayList<>();
            List<Node> toAssign = new ArrayList<>();

            for(Node node : script.getMainNode()) {
                if(node.getKey() == null) continue;
                if(node.getKey().charAt(0) == '@') {
                    toRemove.add(node);
                    toAssign.add(node);
                    continue;
                }
                SkriptParser.ParseResult skriptClassParse = SkriptReflection.parse_i(
                        new SkriptParser(node.getKey(), SkriptParser.PARSE_LITERALS, ParseContext.EVENT),
                        PATTERN, 0, 0);
                if(skriptClassParse == null) continue;
                String className = skriptClassParse.regexes.get(0).group();
                CLASS_ANNOTATION_MAP.computeIfAbsent(script, k -> new HashMap<>());
                CLASS_ANNOTATION_MAP.get(script).computeIfAbsent(className, k -> new ArrayList<>());

                for(Node annotationNode : toAssign) {
                    CLASS_ANNOTATION_MAP.get(script).get(className).add(annotationNode);
                }

                toAssign.clear();
            }
            for(Node annotationNode : toRemove) {
                script.getMainNode().remove(annotationNode);
            }

        }
    }

}
