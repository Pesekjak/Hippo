package me.pesekjak.hippo.skript;

import ch.njol.skript.lang.parser.ParserInstance;
import me.pesekjak.hippo.core.loader.ClassUpdate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Parser instance data waiting for enrolling next class update after parsing is finished.
 */
public class ParserData extends ParserInstance.Data {

    public ParserData(ParserInstance parserInstance) {
        super(parserInstance);
    }

    @Override
    public void onCurrentEventsChange(Class<? extends Event> @Nullable [] currentEvents) {
        if (currentEvents != null || ClassUpdate.get().getSignatures().isEmpty()) return;
        ClassUpdate.enroll();
    }

}
