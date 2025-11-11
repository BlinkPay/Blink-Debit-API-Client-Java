package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.Refund;
import nz.co.blink.debit.dto.v1.RefundDetail;
import nz.co.blink.debit.dto.v1.RefundResponse;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.HttpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Client for refund operations.
 */
public class RefundsApiClient {

    private static final Logger log = LoggerFactory.getLogger(RefundsApiClient.class);
    private static final String REFUNDS_PATH = "/refunds";

    private final HttpClientHelper httpHelper;

    public RefundsApiClient(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    /**
     * Create a refund.
     *
     * @param request the refund request
     * @return the refund response
     * @throws BlinkServiceException if the request fails
     */
    public RefundResponse createRefund(RefundDetail request) throws BlinkServiceException {
        return createRefund(request, UUID.randomUUID().toString());
    }

    /**
     * Create a refund with custom request ID.
     *
     * @param request   the refund request
     * @param requestId the request ID for tracing
     * @return the refund response
     * @throws BlinkServiceException if the request fails
     */
    public RefundResponse createRefund(RefundDetail request, String requestId) throws BlinkServiceException {
        if (request == null) {
            throw new BlinkInvalidValueException("Refund request must not be null");
        }

        log.debug("Creating refund with request-id: {}", requestId);
        return httpHelper.post(REFUNDS_PATH, request, RefundResponse.class, requestId);
    }

    /**
     * Get a refund by ID.
     *
     * @param refundId the refund ID
     * @return the refund
     * @throws BlinkServiceException if the request fails
     */
    public Refund getRefund(UUID refundId) throws BlinkServiceException {
        return getRefund(refundId, UUID.randomUUID().toString());
    }

    /**
     * Get a refund by ID with custom request ID.
     *
     * @param refundId  the refund ID
     * @param requestId the request ID for tracing
     * @return the refund
     * @throws BlinkServiceException if the request fails
     */
    public Refund getRefund(UUID refundId, String requestId) throws BlinkServiceException {
        if (refundId == null) {
            throw new BlinkInvalidValueException("Refund ID must not be null");
        }

        String path = REFUNDS_PATH + "/" + refundId.toString();
        log.debug("Getting refund {} with request-id: {}", refundId, requestId);
        return httpHelper.get(path, Refund.class, requestId);
    }
}
