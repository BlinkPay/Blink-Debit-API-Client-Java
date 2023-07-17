/**
 * Copyright (c) 2022 BlinkPay
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package nz.co.blink.debit.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * The exception for Blink Debit services.
 */
public class BlinkServiceException extends Exception {

    private BlinkServiceException(String message) {
        super(message);
    }

    private BlinkServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns a generic {@link BlinkServiceException}.
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createServiceException() {
        return createServiceException(null);
    }

    /**
     * Returns a generic {@link BlinkServiceException} with optional error message and {@link Throwable}.
     *
     * @param message the optional error message
     * @param cause   the optional {@link Throwable}s
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createServiceException(final String message, Throwable... cause) {
        Throwable rootCause = null;
        if (cause != null && cause.length > 0) {
            rootCause = cause[0];
        }

        return new BlinkServiceException(StringUtils.defaultString(message,
                "Service call to Blink Debit failed, please contact BlinkPay with the correlation ID"),
                rootCause);
    }

    /**
     * Returns a {@link BlinkServiceException} when the client request is invalid (400).
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createClientException() {
        return new BlinkServiceException("Client request is invalid");
    }

    /**
     * Returns a {@link BlinkServiceException} when the request is unauthorised e.g. expired bearer access token or missing authorization request header (401).
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createUnauthorisedException() {
        return new BlinkServiceException("Unauthorised access to resource, check the JWT in Authorization HTTP request header with Bearer authentication scheme");
    }

    /**
     * Returns a {@link BlinkServiceException} when the merchant does not have sufficient access rights or scopes to a resource (403).
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createForbiddenException() {
        return new BlinkServiceException("Insufficient access right to resource, please contact BlinkPay");
    }

    /**
     * Returns a {@link BlinkServiceException} when the requested resource does not exist or if the resource does not belong to the merchant (404).
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createResourceNotFoundException() {
        return new BlinkServiceException("Resource not found");
    }

    /**
     * Returns a {@link BlinkServiceException} when the number of requests exceeded the limit of the firewall (429).
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createRateLimitExceededException() {
        return new BlinkServiceException("Rate limit exceeded, please contact BlinkPay");
    }

    /**
     * Returns a {@link BlinkServiceException} when Blink Debit encounters a generic error (500).
     *
     * @param correlationId the correlation ID
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createInternalServerErrorException(final String correlationId) {
        return new BlinkServiceException("Internal server error occurred in Blink Debit, please contact BlinkPay with the correlation ID: " + correlationId);
    }

    /**
     * Returns a {@link BlinkServiceException} when the service is not yet implemented (501).
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createServiceNotImplementedException() {
        return new BlinkServiceException("Service not yet implemented");
    }

    /**
     * Returns a {@link BlinkServiceException} when single/domestic or recurring/enduring consent is rejected by the customer.
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createConsentRejectedException() {
        return new BlinkServiceException("Consent was rejected by the customer");
    }

    /**
     * Returns a {@link BlinkServiceException} when consent was not completed within the bank's request timeout window.
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createConsentTimeoutException() {
        return new BlinkServiceException("Consent timed out");
    }

    /**
     * Returns a {@link BlinkServiceException} when single/domestic or recurring/enduring payment is rejected by the bank.
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createPaymentRejectedException() {
        return new BlinkServiceException("Payment was rejected by the bank");
    }

    /**
     * Returns a {@link BlinkServiceException} when payment was not completed within the bank's request timeout window.
     *
     * @return the {@link BlinkServiceException}
     */
    public static BlinkServiceException createPaymentTimeoutException() {
        return new BlinkServiceException("Payment timed out");
    }
}
