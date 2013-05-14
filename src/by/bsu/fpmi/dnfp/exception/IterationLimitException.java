package by.bsu.fpmi.dnfp.exception;

/**
 * @author Svetlana Kostyukovich
 */
public class IterationLimitException extends RuntimeException {
    public IterationLimitException() {
    }

    public IterationLimitException(String message) {
        super(message);
    }

    public IterationLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
