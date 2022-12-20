package nz.co.blink.debit.exception;

/**
 * The exception thrown when the request timed out (408).
 */
public class BlinkRequestTimeoutException extends BlinkServiceException {

    public BlinkRequestTimeoutException() {
        this("Request timed out");
    }

    public BlinkRequestTimeoutException(String message) {
        super(message);
    }

    public BlinkRequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
