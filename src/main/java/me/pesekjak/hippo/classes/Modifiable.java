package me.pesekjak.hippo.classes;

import java.util.ArrayList;
import java.util.List;

public class Modifiable {

    private final List<Modifier> modifiers = new ArrayList<>();

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void addModifier(Modifier modifier) {
        if(!modifiers.contains(modifier)) modifiers.add(modifier);
    }

    public void removeModifier(Modifier modifier) {
        modifiers.remove(modifier);
    }

//    public String modifiersAsString() {
//        StringBuilder stringBuilder = new StringBuilder();
//        for(Modifier modifier : modifiers) {
//            stringBuilder.append(" ").append(modifier.getIdentifier());
//        }
//        return stringBuilder.toString();
//    }

}
