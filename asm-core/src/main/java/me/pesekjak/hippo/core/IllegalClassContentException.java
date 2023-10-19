package me.pesekjak.hippo.core;

/**
 * Exception used when class has illegal content.
 */
public class IllegalClassContentException extends Exception {

    public IllegalClassContentException() {
        super();
    }

    public IllegalClassContentException(String message) {
        super(message);
    }

    public IllegalClassContentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalClassContentException(Throwable cause) {
        super(cause);
    }

}
