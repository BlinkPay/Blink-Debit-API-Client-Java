package nz.co.blink.debit.exception;

/**
 * The exception thrown when the merchant does not have sufficient access rights or scopes to a resource (403).
 */
public class BlinkForbiddenException extends BlinkServiceException {

    public BlinkForbiddenException() {
        this("Insufficient access right to resource, please contact BlinkPay");
    }

    public BlinkForbiddenException(String message) {
        super(message);
    }

    public BlinkForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
