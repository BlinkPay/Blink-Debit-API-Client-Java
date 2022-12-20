package nz.co.blink.debit.exception;

/**
 * The exception thrown when service is not yet implemented (408).
 */
public class BlinkNotImplementedException extends BlinkServiceException {

    public BlinkNotImplementedException() {
        this("Service not yet implemented");
    }

    public BlinkNotImplementedException(String message) {
        super(message);
    }

    public BlinkNotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }
}
