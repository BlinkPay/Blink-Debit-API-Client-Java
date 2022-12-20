package nz.co.blink.debit.exception;

/**
 * The exception thrown when the requested resource does not exist or if the resource does not belong to the merchant (404).
 */
public class BlinkResourceNotFoundException extends BlinkServiceException {

    public BlinkResourceNotFoundException() {
        this("Resource not found");
    }

    public BlinkResourceNotFoundException(String message) {
        super(message);
    }

    public BlinkResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
