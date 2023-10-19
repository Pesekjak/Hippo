package me.pesekjak.hippo.utils.parser;

/**
 * Represents exception generated while parsing text input.
 */
public class ParserException extends Exception {

    public ParserException() {
        super();
    }

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }

}
