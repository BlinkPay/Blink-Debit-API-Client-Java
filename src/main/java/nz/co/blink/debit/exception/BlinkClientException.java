package nz.co.blink.debit.exception;

/**
 * The exception thrown when the client request is invalid.
 */
public class BlinkClientException extends BlinkServiceException {

    public BlinkClientException() {
        this("Client request is invalid");
    }

    public BlinkClientException(String message) {
        super(message);
    }

    public BlinkClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
