package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.HttpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Client for payment operations.
 */
public class PaymentsApiClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentsApiClient.class);
    private static final String PAYMENTS_PATH = "/payments/v1/payments";

    private final HttpClientHelper httpHelper;

    public PaymentsApiClient(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    /**
     * Create a payment.
     *
     * @param request the payment request
     * @return the payment response
     * @throws BlinkServiceException if the request fails
     */
    public PaymentResponse createPayment(PaymentRequest request) throws BlinkServiceException {
        return createPayment(request, UUID.randomUUID().toString());
    }

    /**
     * Create a payment with custom request ID.
     *
     * @param request   the payment request
     * @param requestId the request ID for tracing
     * @return the payment response
     * @throws BlinkServiceException if the request fails
     */
    public PaymentResponse createPayment(PaymentRequest request, String requestId) throws BlinkServiceException {
        if (request == null) {
            throw new BlinkInvalidValueException("Payment request must not be null");
        }

        log.debug("Creating payment with request-id: {}", requestId);
        return httpHelper.post(PAYMENTS_PATH, request, PaymentResponse.class, requestId);
    }

    /**
     * Get a payment by ID.
     *
     * @param paymentId the payment ID
     * @return the payment
     * @throws BlinkServiceException if the request fails
     */
    public Payment getPayment(UUID paymentId) throws BlinkServiceException {
        return getPayment(paymentId, UUID.randomUUID().toString());
    }

    /**
     * Get a payment by ID with custom request ID.
     *
     * @param paymentId the payment ID
     * @param requestId the request ID for tracing
     * @return the payment
     * @throws BlinkServiceException if the request fails
     */
    public Payment getPayment(UUID paymentId, String requestId) throws BlinkServiceException {
        if (paymentId == null) {
            throw new BlinkInvalidValueException("Payment ID must not be null");
        }

        String path = PAYMENTS_PATH + "/" + paymentId.toString();
        log.debug("Getting payment {} with request-id: {}", paymentId, requestId);
        return httpHelper.get(path, Payment.class, requestId);
    }
}
