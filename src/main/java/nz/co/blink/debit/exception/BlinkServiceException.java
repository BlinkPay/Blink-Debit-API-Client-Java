package nz.co.blink.debit.exception;

/**
 * The base checked exception for generic Blink Debit services.
 */
public class BlinkServiceException extends Exception {

    public BlinkServiceException() {
        this("Service call to Blink Debit failed, please contact BlinkPay with the correlation ID");
    }

    public BlinkServiceException(String message) {
        super(message);
    }

    public BlinkServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
