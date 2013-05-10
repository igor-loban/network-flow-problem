package by.bsu.fpmi.cnfp.exception;

/**
 * @author Igor Loban
 */
public class AntitheticalConstraints extends RuntimeException {
    public AntitheticalConstraints() {
    }

    public AntitheticalConstraints(String message) {
        super(message);
    }

    public AntitheticalConstraints(String message, Throwable cause) {
        super(message, cause);
    }
}
