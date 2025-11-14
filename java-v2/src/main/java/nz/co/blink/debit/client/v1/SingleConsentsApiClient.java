package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.HttpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Client for single consent operations.
 */
public class SingleConsentsApiClient {

    private static final Logger log = LoggerFactory.getLogger(SingleConsentsApiClient.class);
    private static final String SINGLE_CONSENTS_PATH = "/payments/v1/single-consents";
    private static final String SINGLE_CONSENTS_ID_PATH = "/payments/v1/single-consents/";

    private final HttpClientHelper httpHelper;

    public SingleConsentsApiClient(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    /**
     * Create a single consent.
     *
     * @param request the single consent request
     * @return the create consent response
     * @throws BlinkServiceException if the request fails
     */
    public CreateConsentResponse createSingleConsent(SingleConsentRequest request) throws BlinkServiceException {
        return createSingleConsent(request, UUID.randomUUID().toString());
    }

    /**
     * Create a single consent with custom request ID.
     *
     * @param request   the single consent request
     * @param requestId the request ID for tracing
     * @return the create consent response
     * @throws BlinkServiceException if the request fails
     */
    public CreateConsentResponse createSingleConsent(SingleConsentRequest request, String requestId)
            throws BlinkServiceException {
        if (request == null) {
            throw new BlinkInvalidValueException("Single consent request must not be null");
        }

        log.debug("Creating single consent with request-id: {}", requestId);
        return httpHelper.post(SINGLE_CONSENTS_PATH, request, CreateConsentResponse.class, requestId);
    }

    /**
     * Get a consent by ID.
     *
     * @param consentId the consent ID
     * @return the consent
     * @throws BlinkServiceException if the request fails
     */
    public Consent getConsent(UUID consentId) throws BlinkServiceException {
        return getConsent(consentId, UUID.randomUUID().toString());
    }

    /**
     * Get a consent by ID with custom request ID.
     *
     * @param consentId the consent ID
     * @param requestId the request ID for tracing
     * @return the consent
     * @throws BlinkServiceException if the request fails
     */
    public Consent getConsent(UUID consentId, String requestId) throws BlinkServiceException {
        if (consentId == null) {
            throw new BlinkInvalidValueException("Consent ID must not be null");
        }

        String path = SINGLE_CONSENTS_ID_PATH + consentId.toString();
        log.debug("Getting consent {} with request-id: {}", consentId, requestId);
        return httpHelper.get(path, Consent.class, requestId);
    }

    /**
     * Revoke a consent.
     *
     * @param consentId the consent ID
     * @throws BlinkServiceException if the request fails
     */
    public void revokeConsent(UUID consentId) throws BlinkServiceException {
        revokeConsent(consentId, UUID.randomUUID().toString());
    }

    /**
     * Revoke a consent with custom request ID.
     *
     * @param consentId the consent ID
     * @param requestId the request ID for tracing
     * @throws BlinkServiceException if the request fails
     */
    public void revokeConsent(UUID consentId, String requestId) throws BlinkServiceException {
        if (consentId == null) {
            throw new BlinkInvalidValueException("Consent ID must not be null");
        }

        String path = SINGLE_CONSENTS_ID_PATH + consentId.toString();
        log.debug("Revoking consent {} with request-id: {}", consentId, requestId);
        httpHelper.delete(path, requestId);
    }
}
