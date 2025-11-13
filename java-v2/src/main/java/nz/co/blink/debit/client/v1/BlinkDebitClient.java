package nz.co.blink.debit.client.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nz.co.blink.debit.config.BlinkDebitConfig;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.helpers.HttpClientHelper;
import nz.co.blink.debit.service.AccessTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Main facade for Blink Debit API client.
 * Provides synchronous access to all API operations.
 * <p>
 * Example usage:
 * <pre>
 * BlinkDebitConfig config = BlinkDebitConfig.builder()
 *     .debitUrl("https://staging.debit.blinkpay.co.nz")
 *     .clientId("your-client-id")
 *     .clientSecret("your-client-secret")
 *     .build();
 *
 * BlinkDebitClient client = new BlinkDebitClient(config);
 * CreateConsentResponse response = client.createSingleConsent(request);
 * </pre>
 */
public class BlinkDebitClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(BlinkDebitClient.class);

    private final BlinkDebitConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AccessTokenManager tokenManager;
    private final HttpClientHelper httpHelper;

    // API clients
    private final OAuthApiClient oauthApi;
    private final SingleConsentsApiClient singleConsentsApi;
    private final EnduringConsentsApiClient enduringConsentsApi;
    private final QuickPaymentsApiClient quickPaymentsApi;
    private final PaymentsApiClient paymentsApi;
    private final RefundsApiClient refundsApi;
    private final MetaApiClient metaApi;

    /**
     * Create a client from environment variables.
     * Looks for: BLINKPAY_DEBIT_URL, BLINKPAY_CLIENT_ID, BLINKPAY_CLIENT_SECRET
     *
     * @throws BlinkInvalidValueException if required config is missing
     */
    public BlinkDebitClient() throws BlinkInvalidValueException {
        this(BlinkDebitConfig.fromEnvironment().build());
    }

    /**
     * Create a client with direct parameters.
     * Compatible with v1 SDK API for easy migration.
     *
     * @param debitUrl the Blink Debit API base URL
     * @param clientId the OAuth2 client ID
     * @param clientSecret the OAuth2 client secret
     * @throws BlinkInvalidValueException if required config is missing
     */
    public BlinkDebitClient(String debitUrl, String clientId, String clientSecret)
            throws BlinkInvalidValueException {
        this(BlinkDebitConfig.builder()
                .debitUrl(debitUrl)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build());
    }

    /**
     * Create a client with the specified configuration.
     *
     * @param config the configuration
     */
    public BlinkDebitClient(BlinkDebitConfig config) {
        this.config = config;

        // Create HTTP client with connection pool
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(config.getTimeout())
                .build();

        // Create Jackson ObjectMapper
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create OAuth client and token manager
        this.oauthApi = new OAuthApiClient(httpClient, config, objectMapper);
        this.tokenManager = new AccessTokenManager(oauthApi);

        // Create HTTP helper
        this.httpHelper = new HttpClientHelper(httpClient, config, objectMapper, tokenManager);

        // Initialize API clients
        this.singleConsentsApi = new SingleConsentsApiClient(httpHelper);
        this.enduringConsentsApi = new EnduringConsentsApiClient(httpHelper);
        this.quickPaymentsApi = new QuickPaymentsApiClient(httpHelper);
        this.paymentsApi = new PaymentsApiClient(httpHelper);
        this.refundsApi = new RefundsApiClient(httpHelper);
        this.metaApi = new MetaApiClient(httpHelper);

        log.info("BlinkDebitClient initialized for {}", config.getDebitUrl());
    }

    /**
     * Get the OAuth API client.
     */
    public OAuthApiClient getOAuthApi() {
        return oauthApi;
    }

    /**
     * Get the single consents API client.
     */
    public SingleConsentsApiClient getSingleConsentsApi() {
        return singleConsentsApi;
    }

    /**
     * Get the enduring consents API client.
     */
    public EnduringConsentsApiClient getEnduringConsentsApi() {
        return enduringConsentsApi;
    }

    /**
     * Get the quick payments API client.
     */
    public QuickPaymentsApiClient getQuickPaymentsApi() {
        return quickPaymentsApi;
    }

    /**
     * Get the payments API client.
     */
    public PaymentsApiClient getPaymentsApi() {
        return paymentsApi;
    }

    /**
     * Get the refunds API client.
     */
    public RefundsApiClient getRefundsApi() {
        return refundsApi;
    }

    /**
     * Get the metadata API client.
     */
    public MetaApiClient getMetaApi() {
        return metaApi;
    }

    /**
     * Get the configuration.
     */
    public BlinkDebitConfig getConfig() {
        return config;
    }

    // ========================================================================
    // Convenience Methods - Direct API access without requiring getXxxApi()
    // ========================================================================

    /**
     * Get bank metadata.
     * Convenience method equivalent to getMetaApi().getMeta().
     *
     * @return list of bank metadata
     * @throws nz.co.blink.debit.exception.BlinkServiceException if API call fails
     */
    public java.util.List<nz.co.blink.debit.dto.v1.BankMetadata> getMeta()
            throws nz.co.blink.debit.exception.BlinkServiceException {
        return metaApi.getMeta();
    }

    /**
     * Create a single consent.
     * Convenience method equivalent to getSingleConsentsApi().createSingleConsent().
     *
     * @param request the single consent request
     * @return the consent creation response
     * @throws nz.co.blink.debit.exception.BlinkServiceException if API call fails
     */
    public nz.co.blink.debit.dto.v1.CreateConsentResponse createSingleConsent(
            nz.co.blink.debit.dto.v1.SingleConsentRequest request)
            throws nz.co.blink.debit.exception.BlinkServiceException {
        return singleConsentsApi.createSingleConsent(request);
    }

    /**
     * Get a single consent by ID.
     * Convenience method equivalent to getSingleConsentsApi().getConsent().
     *
     * @param consentId the consent ID
     * @return the consent
     * @throws nz.co.blink.debit.exception.BlinkServiceException if API call fails
     */
    public nz.co.blink.debit.dto.v1.Consent getSingleConsent(java.util.UUID consentId)
            throws nz.co.blink.debit.exception.BlinkServiceException {
        return singleConsentsApi.getConsent(consentId);
    }

    /**
     * Create a quick payment.
     * Convenience method equivalent to getQuickPaymentsApi().createQuickPayment().
     *
     * @param request the quick payment request
     * @return the quick payment creation response
     * @throws nz.co.blink.debit.exception.BlinkServiceException if API call fails
     */
    public nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse createQuickPayment(
            nz.co.blink.debit.dto.v1.QuickPaymentRequest request)
            throws nz.co.blink.debit.exception.BlinkServiceException {
        return quickPaymentsApi.createQuickPayment(request);
    }

    /**
     * Get a quick payment by ID.
     * Convenience method equivalent to getQuickPaymentsApi().getQuickPayment().
     *
     * @param quickPaymentId the quick payment ID
     * @return the quick payment response
     * @throws nz.co.blink.debit.exception.BlinkServiceException if API call fails
     */
    public nz.co.blink.debit.dto.v1.QuickPaymentResponse getQuickPayment(java.util.UUID quickPaymentId)
            throws nz.co.blink.debit.exception.BlinkServiceException {
        return quickPaymentsApi.getQuickPayment(quickPaymentId);
    }

    /**
     * Poll for a successful quick payment within the specified time.
     * Compatible with v1 SDK API.
     * <p>
     * Polls every second until the quick payment consent is AUTHORISED or CONSUMED,
     * or until the timeout is reached. Attempts to revoke the quick payment on timeout.
     *
     * @param quickPaymentId the quick payment ID
     * @param maxWaitSeconds the maximum number of seconds to wait
     * @return the successful quick payment response
     * @throws nz.co.blink.debit.exception.BlinkServiceException if API call fails
     * @throws RuntimeException wrapping timeout or rejection exceptions
     */
    public nz.co.blink.debit.dto.v1.QuickPaymentResponse awaitSuccessfulQuickPaymentOrThrowException(
            java.util.UUID quickPaymentId, int maxWaitSeconds)
            throws nz.co.blink.debit.exception.BlinkServiceException {
        long endTime = System.currentTimeMillis() + (maxWaitSeconds * 1000L);

        while (System.currentTimeMillis() < endTime) {
            nz.co.blink.debit.dto.v1.QuickPaymentResponse response = getQuickPayment(quickPaymentId);
            nz.co.blink.debit.dto.v1.Consent.StatusEnum status = response.getConsent().getStatus();

            log.debug("Polling quick payment {}: consent status = {}", quickPaymentId, status);

            if (status == nz.co.blink.debit.dto.v1.Consent.StatusEnum.AUTHORISED ||
                status == nz.co.blink.debit.dto.v1.Consent.StatusEnum.CONSUMED) {
                return response;
            }

            if (status == nz.co.blink.debit.dto.v1.Consent.StatusEnum.REJECTED ||
                status == nz.co.blink.debit.dto.v1.Consent.StatusEnum.REVOKED) {
                throw new RuntimeException("Quick payment " + quickPaymentId + " was rejected or revoked");
            }

            try {
                Thread.sleep(1000); // Poll every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new nz.co.blink.debit.exception.BlinkServiceException("Polling interrupted", e);
            }
        }

        // Timeout - attempt to revoke
        log.warn("Quick payment {} timed out after {} seconds, attempting to revoke",
                quickPaymentId, maxWaitSeconds);
        try {
            quickPaymentsApi.revokeQuickPayment(quickPaymentId);
            log.info("Revoked quick payment {} after timeout", quickPaymentId);
        } catch (Exception e) {
            log.error("Failed to revoke quick payment {} after timeout", quickPaymentId, e);
        }

        throw new RuntimeException("Timed out waiting for quick payment " + quickPaymentId +
                " to complete after " + maxWaitSeconds + " seconds");
    }

    /**
     * Close the client and release resources.
     */
    @Override
    public void close() {
        log.info("BlinkDebitClient closed");
        // HttpClient doesn't need explicit closing in Java 11
    }
}
