package by.bsu.fpmi.dnfp.exception;

/**
 * @author Igor Loban
 */
public class AntitheticalConstraintsException extends RuntimeException {
    public AntitheticalConstraintsException() {
    }

    public AntitheticalConstraintsException(String message) {
        super(message);
    }

    public AntitheticalConstraintsException(String message, Throwable cause) {
        super(message, cause);
    }
}
