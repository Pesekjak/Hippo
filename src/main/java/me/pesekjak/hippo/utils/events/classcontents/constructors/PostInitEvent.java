package me.pesekjak.hippo.utils.events.classcontents.constructors;

import me.pesekjak.hippo.utils.events.classcontents.ConstructorEvent;

public class PostInitEvent extends ConstructorEvent {

    public PostInitEvent(Object instance) {
        super(instance);
    }

}
