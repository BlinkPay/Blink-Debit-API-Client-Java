package nz.co.blink.debit.exception;

/**
 * The exception when the request is unauthorised e.g. expired bearer access token or missing authorization request header (401).
 */
public class BlinkUnauthorisedException extends BlinkServiceException {

    public BlinkUnauthorisedException() {
        this("Unauthorised access to resource, check the JWT in Authorization HTTP request header with Bearer authentication scheme");
    }

    public BlinkUnauthorisedException(String message) {
        super(message);
    }

    public BlinkUnauthorisedException(String message, Throwable cause) {
        super(message, cause);
    }
}
