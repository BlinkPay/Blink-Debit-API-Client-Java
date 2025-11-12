package nz.co.blink.debit.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionTest {

    // ===== BlinkServiceException Tests =====

    @Test
    void testBlinkServiceExceptionWithMessage() {
        String message = "Service unavailable";

        BlinkServiceException exception = new BlinkServiceException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testBlinkServiceExceptionWithMessageAndCause() {
        String message = "Request failed";
        RuntimeException cause = new RuntimeException("Network error");

        BlinkServiceException exception = new BlinkServiceException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testBlinkServiceExceptionCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new BlinkServiceException("Test error");
        })
                .isInstanceOf(BlinkServiceException.class)
                .hasMessage("Test error");
    }

    @Test
    void testBlinkServiceExceptionCausePreserved() {
        RuntimeException originalCause = new RuntimeException("Original error");

        assertThatThrownBy(() -> {
            throw new BlinkServiceException("Wrapped error", originalCause);
        })
                .isInstanceOf(BlinkServiceException.class)
                .hasMessage("Wrapped error")
                .hasCause(originalCause);
    }

    // ===== BlinkInvalidValueException Tests =====

    @Test
    void testBlinkInvalidValueExceptionWithMessage() {
        String message = "Invalid parameter";

        BlinkInvalidValueException exception = new BlinkInvalidValueException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testBlinkInvalidValueExceptionWithMessageAndCause() {
        String message = "Validation failed";
        IllegalArgumentException cause = new IllegalArgumentException("Invalid format");

        BlinkInvalidValueException exception = new BlinkInvalidValueException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testBlinkInvalidValueExceptionCanBeThrown() {
        assertThatThrownBy(() -> {
            throw new BlinkInvalidValueException("Invalid input");
        })
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessage("Invalid input");
    }

    @Test
    void testBlinkInvalidValueExceptionExtendsBlinkClientException() {
        BlinkInvalidValueException exception = new BlinkInvalidValueException("Test");

        assertThat(exception).isInstanceOf(BlinkClientException.class);
    }

    // ===== BlinkResourceNotFoundException Tests =====

    @Test
    void testBlinkResourceNotFoundExceptionWithMessage() {
        String message = "Resource not found";

        BlinkResourceNotFoundException exception = new BlinkResourceNotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testBlinkResourceNotFoundExceptionExtendsBlinkServiceException() {
        BlinkResourceNotFoundException exception = new BlinkResourceNotFoundException("Test");

        assertThat(exception).isInstanceOf(BlinkServiceException.class);
    }

    // ===== BlinkUnauthorisedException Tests =====

    @Test
    void testBlinkUnauthorisedExceptionWithMessage() {
        String message = "Unauthorized access";

        BlinkUnauthorisedException exception = new BlinkUnauthorisedException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testBlinkUnauthorisedExceptionExtendsBlinkServiceException() {
        BlinkUnauthorisedException exception = new BlinkUnauthorisedException("Test");

        assertThat(exception).isInstanceOf(BlinkServiceException.class);
    }

    // ===== BlinkForbiddenException Tests =====

    @Test
    void testBlinkForbiddenExceptionWithMessage() {
        String message = "Forbidden operation";

        BlinkForbiddenException exception = new BlinkForbiddenException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testBlinkForbiddenExceptionExtendsBlinkServiceException() {
        BlinkForbiddenException exception = new BlinkForbiddenException("Test");

        assertThat(exception).isInstanceOf(BlinkServiceException.class);
    }

    // ===== BlinkRateLimitExceededException Tests =====

    @Test
    void testBlinkRateLimitExceededExceptionWithMessage() {
        String message = "Rate limit exceeded";

        BlinkRateLimitExceededException exception = new BlinkRateLimitExceededException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testBlinkRateLimitExceededExceptionExtendsBlinkServiceException() {
        BlinkRateLimitExceededException exception = new BlinkRateLimitExceededException("Test");

        assertThat(exception).isInstanceOf(BlinkServiceException.class);
    }

    // ===== BlinkConsentTimeoutException Tests =====

    @Test
    void testBlinkConsentTimeoutExceptionWithMessage() {
        String message = "Consent timed out";

        BlinkConsentTimeoutException exception = new BlinkConsentTimeoutException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testBlinkConsentTimeoutExceptionExtendsBlinkConsentFailureException() {
        BlinkConsentTimeoutException exception = new BlinkConsentTimeoutException("Test");

        assertThat(exception).isInstanceOf(BlinkConsentFailureException.class);
    }

    // ===== BlinkPaymentRejectedException Tests =====

    @Test
    void testBlinkPaymentRejectedExceptionWithMessage() {
        String message = "Payment rejected";

        BlinkPaymentRejectedException exception = new BlinkPaymentRejectedException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testBlinkPaymentRejectedExceptionExtendsBlinkPaymentFailureException() {
        BlinkPaymentRejectedException exception = new BlinkPaymentRejectedException("Test");

        assertThat(exception).isInstanceOf(BlinkPaymentFailureException.class);
    }

    // ===== Exception Hierarchy Tests =====

    @Test
    void testExceptionHierarchy() {
        // BlinkServiceException extends Exception
        assertThat(new BlinkServiceException("test"))
                .isInstanceOf(Exception.class);

        // BlinkClientException extends BlinkServiceException
        assertThat(new BlinkClientException("test"))
                .isInstanceOf(BlinkServiceException.class)
                .isInstanceOf(Exception.class);

        // BlinkInvalidValueException extends BlinkClientException
        assertThat(new BlinkInvalidValueException("test"))
                .isInstanceOf(BlinkClientException.class)
                .isInstanceOf(BlinkServiceException.class)
                .isInstanceOf(Exception.class);

        // BlinkResourceNotFoundException extends BlinkServiceException
        assertThat(new BlinkResourceNotFoundException("test"))
                .isInstanceOf(BlinkServiceException.class)
                .isInstanceOf(Exception.class);

        // BlinkRetryableException extends Exception (separate hierarchy)
        assertThat(new BlinkRetryableException("test"))
                .isInstanceOf(Exception.class);
    }

    // ===== Exception Message Tests =====

    @Test
    void testExceptionMessagesArePreserved() {
        String message1 = "Error message 1";
        String message2 = "Error message 2";
        String message3 = "Error message 3";

        assertThat(new BlinkServiceException(message1).getMessage()).isEqualTo(message1);
        assertThat(new BlinkInvalidValueException(message2).getMessage()).isEqualTo(message2);
        assertThat(new BlinkResourceNotFoundException(message3).getMessage()).isEqualTo(message3);
    }

    // ===== Exception instanceof Tests =====

    @Test
    void testInstanceofChecks() {
        Exception serviceException = new BlinkServiceException("test");
        Exception clientException = new BlinkClientException("test");
        Exception invalidValueException = new BlinkInvalidValueException("test");
        Exception resourceNotFoundException = new BlinkResourceNotFoundException("test");

        assertThat(serviceException instanceof BlinkServiceException).isTrue();
        assertThat(serviceException instanceof Exception).isTrue();

        assertThat(clientException instanceof BlinkClientException).isTrue();
        assertThat(clientException instanceof BlinkServiceException).isTrue();
        assertThat(clientException instanceof Exception).isTrue();

        assertThat(invalidValueException instanceof BlinkInvalidValueException).isTrue();
        assertThat(invalidValueException instanceof BlinkClientException).isTrue();
        assertThat(invalidValueException instanceof BlinkServiceException).isTrue();

        assertThat(resourceNotFoundException instanceof BlinkResourceNotFoundException).isTrue();
        assertThat(resourceNotFoundException instanceof BlinkServiceException).isTrue();
        assertThat(resourceNotFoundException instanceof Exception).isTrue();
    }
}
