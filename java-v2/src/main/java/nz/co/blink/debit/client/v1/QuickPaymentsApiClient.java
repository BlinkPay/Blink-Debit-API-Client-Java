package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.QuickPaymentRequest;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.HttpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Client for quick payment operations.
 */
public class QuickPaymentsApiClient {

    private static final Logger log = LoggerFactory.getLogger(QuickPaymentsApiClient.class);
    private static final String QUICK_PAYMENTS_PATH = "/consents/quick-payments";

    private final HttpClientHelper httpHelper;

    public QuickPaymentsApiClient(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    /**
     * Create a quick payment (consent + payment in one call).
     *
     * @param request the quick payment request
     * @return the create quick payment response
     * @throws BlinkServiceException if the request fails
     */
    public CreateQuickPaymentResponse createQuickPayment(QuickPaymentRequest request) throws BlinkServiceException {
        return createQuickPayment(request, UUID.randomUUID().toString());
    }

    /**
     * Create a quick payment with custom request ID.
     *
     * @param request   the quick payment request
     * @param requestId the request ID for tracing
     * @return the create quick payment response
     * @throws BlinkServiceException if the request fails
     */
    public CreateQuickPaymentResponse createQuickPayment(QuickPaymentRequest request, String requestId)
            throws BlinkServiceException {
        if (request == null) {
            throw new BlinkInvalidValueException("Quick payment request must not be null");
        }

        log.debug("Creating quick payment with request-id: {}", requestId);
        return httpHelper.post(QUICK_PAYMENTS_PATH, request, CreateQuickPaymentResponse.class, requestId);
    }

    /**
     * Get a quick payment by ID.
     *
     * @param quickPaymentId the quick payment ID
     * @return the quick payment response
     * @throws BlinkServiceException if the request fails
     */
    public QuickPaymentResponse getQuickPayment(UUID quickPaymentId) throws BlinkServiceException {
        return getQuickPayment(quickPaymentId, UUID.randomUUID().toString());
    }

    /**
     * Get a quick payment by ID with custom request ID.
     *
     * @param quickPaymentId the quick payment ID
     * @param requestId      the request ID for tracing
     * @return the quick payment response
     * @throws BlinkServiceException if the request fails
     */
    public QuickPaymentResponse getQuickPayment(UUID quickPaymentId, String requestId) throws BlinkServiceException {
        if (quickPaymentId == null) {
            throw new BlinkInvalidValueException("Quick payment ID must not be null");
        }

        String path = QUICK_PAYMENTS_PATH + "/" + quickPaymentId.toString();
        log.debug("Getting quick payment {} with request-id: {}", quickPaymentId, requestId);
        return httpHelper.get(path, QuickPaymentResponse.class, requestId);
    }

    /**
     * Revoke a quick payment.
     *
     * @param quickPaymentId the quick payment ID
     * @throws BlinkServiceException if the request fails
     */
    public void revokeQuickPayment(UUID quickPaymentId) throws BlinkServiceException {
        revokeQuickPayment(quickPaymentId, UUID.randomUUID().toString());
    }

    /**
     * Revoke a quick payment with custom request ID.
     *
     * @param quickPaymentId the quick payment ID
     * @param requestId      the request ID for tracing
     * @throws BlinkServiceException if the request fails
     */
    public void revokeQuickPayment(UUID quickPaymentId, String requestId) throws BlinkServiceException {
        if (quickPaymentId == null) {
            throw new BlinkInvalidValueException("Quick payment ID must not be null");
        }

        String path = QUICK_PAYMENTS_PATH + "/" + quickPaymentId.toString();
        log.debug("Revoking quick payment {} with request-id: {}", quickPaymentId, requestId);
        httpHelper.delete(path, requestId);
    }
}
