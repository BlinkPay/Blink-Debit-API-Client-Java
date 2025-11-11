package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.HttpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Client for enduring consent operations.
 */
public class EnduringConsentsApiClient {

    private static final Logger log = LoggerFactory.getLogger(EnduringConsentsApiClient.class);
    private static final String ENDURING_CONSENTS_PATH = "/consents/enduring";
    private static final String CONSENTS_PATH = "/consents/";

    private final HttpClientHelper httpHelper;

    public EnduringConsentsApiClient(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    /**
     * Create an enduring consent.
     *
     * @param request the enduring consent request
     * @return the create consent response
     * @throws BlinkServiceException if the request fails
     */
    public CreateConsentResponse createEnduringConsent(EnduringConsentRequest request) throws BlinkServiceException {
        return createEnduringConsent(request, UUID.randomUUID().toString());
    }

    /**
     * Create an enduring consent with custom request ID.
     *
     * @param request   the enduring consent request
     * @param requestId the request ID for tracing
     * @return the create consent response
     * @throws BlinkServiceException if the request fails
     */
    public CreateConsentResponse createEnduringConsent(EnduringConsentRequest request, String requestId)
            throws BlinkServiceException {
        if (request == null) {
            throw new BlinkInvalidValueException("Enduring consent request must not be null");
        }

        log.debug("Creating enduring consent with request-id: {}", requestId);
        return httpHelper.post(ENDURING_CONSENTS_PATH, request, CreateConsentResponse.class, requestId);
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

        String path = CONSENTS_PATH + consentId.toString();
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

        String path = CONSENTS_PATH + consentId.toString();
        log.debug("Revoking consent {} with request-id: {}", consentId, requestId);
        httpHelper.delete(path, requestId);
    }
}
