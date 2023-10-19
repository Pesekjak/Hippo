package me.pesekjak.hippo.core;

/**
 * Exception used when modifiable object has illegal modifier combination.
 */
public class IllegalModifiersException extends Exception {

    public IllegalModifiersException() {
        super();
    }

    public IllegalModifiersException(String message) {
        super(message);
    }

    public IllegalModifiersException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalModifiersException(Throwable cause) {
        super(cause);
    }

}
