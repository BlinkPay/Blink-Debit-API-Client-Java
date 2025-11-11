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
 *     .debitUrl("https://sandbox.debit.blinkpay.co.nz")
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
        OAuthApiClient oauthApi = new OAuthApiClient(httpClient, config, objectMapper);
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

    /**
     * Close the client and release resources.
     */
    @Override
    public void close() {
        log.info("BlinkDebitClient closed");
        // HttpClient doesn't need explicit closing in Java 11
    }
}
