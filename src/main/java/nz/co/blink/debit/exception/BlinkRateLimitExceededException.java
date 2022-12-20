package nz.co.blink.debit.exception;

/**
 * The exception thrown when the number of requests exceeded the limit of the firewall (429).
 */
public class BlinkRateLimitExceededException extends BlinkServiceException {

    public BlinkRateLimitExceededException() {
        this("Rate limit exceeded, please contact BlinkPay");
    }

    public BlinkRateLimitExceededException(String message) {
        super(message);
    }

    public BlinkRateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
