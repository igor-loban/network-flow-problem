package by.bsu.fpmi.dnfp.exception;

/**
 * @author Igor Loban
 */
public class LogicalFailException extends RuntimeException {
    public LogicalFailException() {
    }

    public LogicalFailException(String message) {
        super(message);
    }

    public LogicalFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
