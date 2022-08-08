package me.pesekjak.hippo.classes.content;

import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.IClassBuilder;

public interface ClassContent {

    /**
     * Identifier of the class content to prevent conflicts and duplicates.
     * @return Identifier of the class content
     */
    String getIdentifier();

    /**
     * Name of the class content.
     * @return name of the class content
     */
    String getName();

    /**
     * Returns return Type of the class content.
     * @return Type of the class content
     */
    Type getType();

    /**
     * Returns descriptor of the class content.
     * @return Descriptor of the class content
     */
    String getDescriptor();

    /**
     * Setups the class content for the ClassBuilder.
     * @param CB ClassBuilder building the class
     */
    void setup(final IClassBuilder CB);

}
