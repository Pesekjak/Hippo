package me.pesekjak.hippo.skript;

import me.pesekjak.hippo.core.loader.ClassUpdate;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptEvent;

/**
 * ScriptInactive listener waiting for enrolling next class update after all
 * parsing scripts are made inactive by the parser.
 */
public class ScriptInactiveEvent implements ScriptEvent.ScriptInactiveEvent {

    @Override
    public void onInactive(@Nullable Script newScript) {
        if (newScript != null || ClassUpdate.get().getSignatures().size() == 0) return;
        ClassUpdate.enroll();
    }

}
