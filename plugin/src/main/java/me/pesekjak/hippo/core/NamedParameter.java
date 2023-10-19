package me.pesekjak.hippo.core;

import java.util.Objects;

/**
 * Represents a named parameter.
 *
 * @param name name of the parameter
 * @param parameter wrapped parameter
 */
public record NamedParameter(String name, Parameter parameter) {

    public NamedParameter {
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameter);
    }

}
