package tech.geekcity.blackhole.core.exception;

import javax.annotation.Nonnull;

public class BugException extends RuntimeException {
    public BugException(String message) {
        super(message);
    }

    public BugException(String message, Throwable cause) {
        super(message, cause);
    }

    public BugException(Throwable cause) {
        super(cause);
    }

    @Nonnull
    public static BugException wrap(Exception exception) {
        return new BugException(
                String.format(
                        "this exception should not be thrown, please report as a bug: %s",
                        exception.getMessage()),
                exception);
    }
}
