package me.pesekjak.hippo.elements.special;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import me.pesekjak.hippo.Hippo;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.*;

@Name("Class Annotations")
@Description("Adds annotations to custom classes.")
@Examples({
        "@HelloWorld(value = true)",
        "public class Blob:"
})
@Since("1.0.0")
public class SpecClassAnnotations implements Listener {

    static {
        Bukkit.getPluginManager().registerEvents(new SpecClassAnnotations(), Hippo.getInstance());
    }

    /**
     * Moves all top section nodes that match the annotations pattern
     * to the beginning of the next section node.
     *
     * @param event event
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onPreScriptLoad(PreScriptLoadEvent event) {
        for (Config script : event.getScripts()) {
            File file = script.getFile();
            if (file == null) continue;

            SectionNode main = script.getMainNode();
            if (main.isEmpty()) return;

            List<RemovedNode> all = new ArrayList<>();
            List<RemovedNode> nextRemoved = new ArrayList<>();

            for (Node next : main) {
                String key = next.getKey();
                if (key == null) continue;
                key = key.trim();
                if (key.length() == 0) continue;

                if (key.charAt(0) != '@' || key.charAt(key.length() - 1) == ':') {
                    if (nextRemoved.size() == 0) continue;
                    String toAdd = String.join(", ", nextRemoved.stream().map(node -> node.original.getKey()).toList());
                    next.rename(toAdd + " " + next.getKey());
                    all.addAll(nextRemoved);
                    nextRemoved.clear();
                    continue;
                }

                RemovedNode node = new RemovedNode(next, key);
                nextRemoved.add(node);
            }

            all.forEach(node -> main.remove(node.original));
        }
    }

    /**
     * Represents a node that has been removed.
     *
     * @param original reference for the removed node
     * @param key key of the node
     */
    private record RemovedNode(Node original, String key) {
    }

}
