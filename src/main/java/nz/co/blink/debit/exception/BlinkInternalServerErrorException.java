package nz.co.blink.debit.exception;

/**
 * The exception thrown when Blink Debit encounters a generic error (500).
 */
public class BlinkInternalServerErrorException extends BlinkServiceException {

    public BlinkInternalServerErrorException() {
        this("Internal server error occurred in Blink Debit, please contact BlinkPay with the correlation ID");
    }

    public BlinkInternalServerErrorException(String message) {
        super(message);
    }

    public BlinkInternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
