package me.pesekjak.hippo.utils;

/**
 * Util that allows to throw any exception.
 */
public final class ExceptionThrower {

    private ExceptionThrower() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void throwException(Throwable exception) throws T {
        throw (T) exception;
    }

}
