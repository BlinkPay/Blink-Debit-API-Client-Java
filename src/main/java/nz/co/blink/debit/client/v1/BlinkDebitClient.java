package nz.co.blink.debit.client.v1;

import io.netty.handler.logging.LogLevel;
import nz.co.blink.debit.dto.v1.AccountNumberRefundRequest;
import nz.co.blink.debit.dto.v1.BankMetadata;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.FullRefundRequest;
import nz.co.blink.debit.dto.v1.PartialRefundRequest;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.QuickPaymentRequest;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.dto.v1.Refund;
import nz.co.blink.debit.dto.v1.RefundResponse;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The facade for accessing all client methods from one place.
 */
@Component
public class BlinkDebitClient {

    private final SingleConsentsApiClient singleConsentsApiClient;

    private final EnduringConsentsApiClient enduringConsentsApiClient;

    private final QuickPaymentsApiClient quickPaymentsApiClient;

    private final PaymentsApiClient paymentsApiClient;

    private final RefundsApiClient refundsApiClient;

    private final MetaApiClient metaApiClient;

    /**
     * Default constructor for Spring-based consumer.
     *
     * @param singleConsentsApiClient   the {@link SingleConsentsApiClient}
     * @param enduringConsentsApiClient the {@link EnduringConsentsApiClient}
     * @param quickPaymentsApiClient    the {@link QuickPaymentsApiClient}
     * @param paymentsApiClient         the {@link PaymentsApiClient}
     * @param refundsApiClient          the {@link RefundsApiClient}
     * @param metaApiClient             the {@link MetaApiClient}
     */
    @Autowired
    public BlinkDebitClient(SingleConsentsApiClient singleConsentsApiClient,
                            EnduringConsentsApiClient enduringConsentsApiClient,
                            QuickPaymentsApiClient quickPaymentsApiClient, PaymentsApiClient paymentsApiClient,
                            RefundsApiClient refundsApiClient, MetaApiClient metaApiClient) {
        this.singleConsentsApiClient = singleConsentsApiClient;
        this.enduringConsentsApiClient = enduringConsentsApiClient;
        this.quickPaymentsApiClient = quickPaymentsApiClient;
        this.paymentsApiClient = paymentsApiClient;
        this.refundsApiClient = refundsApiClient;
        this.metaApiClient = metaApiClient;
    }

    /**
     * Constructor for pure Java application.
     *
     * @param properties the {@link Properties} retrieved from
     */
    public BlinkDebitClient(Properties properties) {
        int maxConnections = Integer.parseInt(properties.getProperty("blinkpay.max.connections", "10"));
        Duration maxIdleTime = Duration.parse(properties.getProperty("blinkpay.max.idle.time", "PT20S"));
        Duration maxLifeTime = Duration.parse(properties.getProperty("blinkpay.max.life.time", "PT60S"));
        Duration pendingAcquireTimeout = Duration.parse(properties.getProperty("blinkpay.pending.acquire.timeout", "PT10S"));
        Duration evictionInterval = Duration.parse(properties.getProperty("blinkpay.eviction.interval", "PT60S"));
        String debitUrl = properties.getProperty("blinkpay.debit.url");
        String clientId = properties.getProperty("blinkpay.client.id");
        String clientSecret = properties.getProperty("blinkpay.client.secret");
        String activeProfile = properties.getProperty("blinkpay.active.profile", "test");

        ConnectionProvider provider = ConnectionProvider.builder("blinkpay-conn-provider")
                .maxConnections(maxConnections)
                .maxIdleTime(maxIdleTime)
                .maxLifeTime(maxLifeTime)
                .pendingAcquireTimeout(pendingAcquireTimeout)
                .evictInBackground(evictionInterval)
                .build();

        HttpClient client;
        boolean debugMode = Pattern.compile("local|dev|test").matcher(activeProfile).matches();
        if (debugMode) {
            client = HttpClient.create(provider)
                    .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        } else {
            client = HttpClient.create(provider);
        }
        client.warmup().subscribe();

        ReactorClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector(client);

        OAuthApiClient oauthApiClient = new OAuthApiClient(reactorClientHttpConnector, debitUrl, clientId, clientSecret);
        AccessTokenHandler accessTokenHandler = new AccessTokenHandler(oauthApiClient);
        singleConsentsApiClient = new SingleConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        enduringConsentsApiClient = new EnduringConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        quickPaymentsApiClient = new QuickPaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        paymentsApiClient = new PaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        refundsApiClient = new RefundsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        metaApiClient = new MetaApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
    }

    /**
     * Constructor for pure Java application.
     *
     * @param debitUrl the Blink Debit URL
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @param activeProfile the active profile
     */
    public BlinkDebitClient(final String debitUrl, final String clientId, final String clientSecret,
                            final String activeProfile) {
        int maxConnections = 10;
        Duration maxIdleTime = Duration.parse("PT20S");
        Duration maxLifeTime = Duration.parse("PT60S");
        Duration pendingAcquireTimeout = Duration.parse("PT10S");
        Duration evictionInterval = Duration.parse("PT60S");

        ConnectionProvider provider = ConnectionProvider.builder("blinkpay-conn-provider")
                .maxConnections(maxConnections)
                .maxIdleTime(maxIdleTime)
                .maxLifeTime(maxLifeTime)
                .pendingAcquireTimeout(pendingAcquireTimeout)
                .evictInBackground(evictionInterval)
                .build();

        HttpClient client;
        boolean debugMode = Pattern.compile("local|dev|test").matcher(activeProfile).matches();
        if (debugMode) {
            client = HttpClient.create(provider)
                    .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        } else {
            client = HttpClient.create(provider);
        }
        client.warmup().subscribe();

        ReactorClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector(client);

        OAuthApiClient oauthApiClient = new OAuthApiClient(reactorClientHttpConnector, debitUrl, clientId, clientSecret);
        AccessTokenHandler accessTokenHandler = new AccessTokenHandler(oauthApiClient);
        singleConsentsApiClient = new SingleConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        enduringConsentsApiClient = new EnduringConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        quickPaymentsApiClient = new QuickPaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        paymentsApiClient = new PaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        refundsApiClient = new RefundsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
        metaApiClient = new MetaApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
    }

    /**
     * Returns the {@link List} of {@link BankMetadata}.
     *
     * @return the {@link List} of {@link BankMetadata}
     */
    public List<BankMetadata> getMeta() {
        return getMetaAsFlux().collectList().block();
    }

    /**
     * Returns the {@link List} of {@link BankMetadata}.
     *
     * @param requestId the optional correlation ID
     * @return the {@link List} of {@link BankMetadata}
     */
    public List<BankMetadata> getMeta(final String requestId) {
        return getMetaAsFlux(requestId).collectList().block();
    }

    /**
     * Returns the {@link BankMetadata} {@link Flux}.
     *
     * @return the {@link BankMetadata} {@link Flux}
     */
    public Flux<BankMetadata> getMetaAsFlux() {
        return metaApiClient.getMeta();
    }

    /**
     * Returns the {@link BankMetadata} {@link Flux}.
     *
     * @param requestId the optional correlation ID
     * @return the {@link BankMetadata} {@link Flux}
     */
    public Flux<BankMetadata> getMetaAsFlux(final String requestId) {
        return metaApiClient.getMeta(requestId);
    }

    /**
     * Creates a single consent with redirect flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createSingleConsentWithRedirectFlow(SingleConsentRequest request) {
        return createSingleConsentWithRedirectFlowAsMono(request).block();
    }

    /**
     * Creates a single consent with redirect flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createSingleConsentWithRedirectFlow(SingleConsentRequest request,
                                                                     final String requestId) {
        return createSingleConsentWithRedirectFlowAsMono(request, requestId).block();
    }

    /**
     * Creates a single consent with redirect flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithRedirectFlowAsMono(SingleConsentRequest request) {
        return singleConsentsApiClient.createSingleConsentWithRedirectFlow(request);
    }

    /**
     * Creates a single consent with redirect flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithRedirectFlowAsMono(SingleConsentRequest request,
                                                                                 final String requestId) {
        return singleConsentsApiClient.createSingleConsentWithRedirectFlow(request, requestId);
    }

    /**
     * Creates a single consent with decoupled flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createSingleConsentWithDecoupledFlow(SingleConsentRequest request) {
        return createSingleConsentWithDecoupledFlowAsMono(request).block();
    }

    /**
     * Creates a single consent with decoupled flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createSingleConsentWithDecoupledFlow(SingleConsentRequest request,
                                                                      final String requestId) {
        return createSingleConsentWithDecoupledFlowAsMono(request, requestId).block();
    }

    /**
     * Creates a single consent with decoupled flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithDecoupledFlowAsMono(SingleConsentRequest request) {
        return singleConsentsApiClient.createSingleConsentWithDecoupledFlow(request);
    }

    /**
     * Creates a single consent with decoupled flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithDecoupledFlowAsMono(SingleConsentRequest request,
                                                                                  final String requestId) {
        return singleConsentsApiClient.createSingleConsentWithDecoupledFlow(request, requestId);
    }

    /**
     * Creates a single consent with gateway flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createSingleConsentWithGatewayFlow(SingleConsentRequest request) {
        return createSingleConsentWithGatewayFlowAsMono(request).block();
    }

    /**
     * Creates a single consent with gateway flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createSingleConsentWithGatewayFlow(SingleConsentRequest request,
                                                                    final String requestId) {
        return createSingleConsentWithGatewayFlowAsMono(request, requestId).block();
    }

    /**
     * Creates a single consent with gateway flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithGatewayFlowAsMono(SingleConsentRequest request) {
        return singleConsentsApiClient.createSingleConsentWithGatewayFlow(request);
    }

    /**
     * Creates a single consent with gateway flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithGatewayFlowAsMono(SingleConsentRequest request,
                                                                                final String requestId) {
        return singleConsentsApiClient.createSingleConsentWithGatewayFlow(request, requestId);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @return the {@link Consent}
     */
    public Consent getSingleConsent(UUID consentId) {
        return getSingleConsentAsMono(consentId).block();
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     * @return the {@link Consent}
     */
    public Consent getSingleConsent(UUID consentId, String requestId) {
        return getSingleConsentAsMono(consentId, requestId).block();
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @return the {@link Consent} {@link Mono}
     */
    public Mono<Consent> getSingleConsentAsMono(UUID consentId) {
        return singleConsentsApiClient.getSingleConsent(consentId);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     * @return the {@link Consent} {@link Mono}
     */
    public Mono<Consent> getSingleConsentAsMono(UUID consentId, String requestId) {
        return singleConsentsApiClient.getSingleConsent(consentId, requestId);
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     */
    public void revokeSingleConsent(UUID consentId) {
        revokeSingleConsentAsMono(consentId).block();
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     */
    public void revokeSingleConsent(UUID consentId, final String requestId) {
        revokeSingleConsentAsMono(consentId, requestId).block();
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     */
    public Mono<Void> revokeSingleConsentAsMono(UUID consentId) {
        return singleConsentsApiClient.revokeSingleConsent(consentId);
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     */
    public Mono<Void> revokeSingleConsentAsMono(UUID consentId, final String requestId) {
        return singleConsentsApiClient.revokeSingleConsent(consentId, requestId);
    }

    /**
     * Creates an enduring consent with redirect flow.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createEnduringConsentWithRedirectFlow(EnduringConsentRequest request) {
        return createEnduringConsentWithRedirectFlowAsMono(request).block();
    }

    /**
     * Creates an enduring consent with redirect flow.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createEnduringConsentWithRedirectFlow(EnduringConsentRequest request,
                                                                       final String requestId) {
        return createEnduringConsentWithRedirectFlowAsMono(request, requestId).block();
    }

    /**
     * Creates an enduring consent with redirect flow.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsentWithRedirectFlowAsMono(EnduringConsentRequest request) {
        return enduringConsentsApiClient.createEnduringConsentWithRedirectFlow(request);
    }

    /**
     * Creates an enduring consent with redirect flow.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsentWithRedirectFlowAsMono(EnduringConsentRequest request,
                                                                                   final String requestId) {
        return enduringConsentsApiClient.createEnduringConsentWithRedirectFlow(request, requestId);
    }

    /**
     * Creates an enduring consent with decoupled flow.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createEnduringConsentWithDecoupledFlow(EnduringConsentRequest request) {
        return createEnduringConsentWithDecoupledFlowAsMono(request).block();
    }

    /**
     * Creates an enduring consent with decoupled flow.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createEnduringConsentWithDecoupledFlow(EnduringConsentRequest request,
                                                                        final String requestId) {
        return createEnduringConsentWithDecoupledFlowAsMono(request, requestId).block();
    }

    /**
     * Creates an enduring consent with decoupled flow.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsentWithDecoupledFlowAsMono(EnduringConsentRequest request) {
        return enduringConsentsApiClient.createEnduringConsentWithDecoupledFlow(request);
    }

    /**
     * Creates an enduring consent with decoupled flow.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsentWithDecoupledFlowAsMono(EnduringConsentRequest request,
                                                                                    final String requestId) {
        return enduringConsentsApiClient.createEnduringConsentWithDecoupledFlow(request, requestId);
    }

    /**
     * Creates an enduring consent with gateway flow.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createEnduringConsentWithGatewayFlow(EnduringConsentRequest request) {
        return createEnduringConsentWithGatewayFlowAsMono(request).block();
    }

    /**
     * Creates an enduring consent with gateway flow.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createEnduringConsentWithGatewayFlow(EnduringConsentRequest request,
                                                                      final String requestId) {
        return createEnduringConsentWithGatewayFlowAsMono(request, requestId).block();
    }

    /**
     * Creates an enduring consent with gateway flow.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsentWithGatewayFlowAsMono(EnduringConsentRequest request) {
        return enduringConsentsApiClient.createEnduringConsentWithGatewayFlow(request);
    }

    /**
     * Creates an enduring consent with gateway flow.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsentWithGatewayFlowAsMono(EnduringConsentRequest request,
                                                                                  final String requestId) {
        return enduringConsentsApiClient.createEnduringConsentWithGatewayFlow(request, requestId);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @return the {@link Consent}
     */
    public Consent getEnduringConsent(UUID consentId) {
        return getEnduringConsentAsMono(consentId).block();
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     * @return the {@link Consent}
     */
    public Consent getEnduringConsent(UUID consentId, String requestId) {
        return getEnduringConsentAsMono(consentId, requestId).block();
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @return the {@link Consent} {@link Mono}
     */
    public Mono<Consent> getEnduringConsentAsMono(UUID consentId) {
        return enduringConsentsApiClient.getEnduringConsent(consentId);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     * @return the {@link Consent} {@link Mono}
     */
    public Mono<Consent> getEnduringConsentAsMono(UUID consentId, String requestId) {
        return enduringConsentsApiClient.getEnduringConsent(consentId, requestId);
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     */
    public void revokeEnduringConsent(UUID consentId) {
        revokeEnduringConsentAsMono(consentId).block();
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     */
    public void revokeEnduringConsent(UUID consentId, final String requestId) {
        revokeEnduringConsentAsMono(consentId, requestId).block();
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     */
    public Mono<Void> revokeEnduringConsentAsMono(UUID consentId) {
        return enduringConsentsApiClient.revokeEnduringConsent(consentId);
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     */
    public Mono<Void> revokeEnduringConsentAsMono(UUID consentId, final String requestId) {
        return enduringConsentsApiClient.revokeEnduringConsent(consentId, requestId);
    }

    /**
     * Creates a quick payment with redirect flow.
     *
     * @param request the {@link QuickPaymentRequest}
     * @return the {@link CreateQuickPaymentResponse}
     */
    public CreateQuickPaymentResponse createQuickPaymentWithRedirectFlow(QuickPaymentRequest request) {
        return createQuickPaymentWithRedirectFlowAsMono(request).block();
    }

    /**
     * Creates a quick payment with redirect flow.
     *
     * @param request   the {@link QuickPaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateQuickPaymentResponse}
     */
    public CreateQuickPaymentResponse createQuickPaymentWithRedirectFlow(QuickPaymentRequest request,
                                                                         final String requestId) {
        return createQuickPaymentWithRedirectFlowAsMono(request, requestId).block();
    }

    /**
     * Creates a quick payment with redirect flow.
     *
     * @param request the {@link QuickPaymentRequest}
     * @return the {@link CreateQuickPaymentResponse} {@link Mono}
     */
    public Mono<CreateQuickPaymentResponse> createQuickPaymentWithRedirectFlowAsMono(QuickPaymentRequest request) {
        return quickPaymentsApiClient.createQuickPaymentWithRedirectFlow(request);
    }

    /**
     * Creates a quick payment with redirect flow.
     *
     * @param request   the {@link QuickPaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateQuickPaymentResponse} {@link Mono}
     */
    public Mono<CreateQuickPaymentResponse> createQuickPaymentWithRedirectFlowAsMono(QuickPaymentRequest request,
                                                                                     final String requestId) {
        return quickPaymentsApiClient.createQuickPaymentWithRedirectFlow(request, requestId);
    }

    /**
     * Creates a quick payment with decoupled flow.
     *
     * @param request the {@link QuickPaymentRequest}
     * @return the {@link CreateQuickPaymentResponse}
     */
    public CreateQuickPaymentResponse createQuickPaymentWithDecoupledFlow(QuickPaymentRequest request) {
        return createQuickPaymentWithDecoupledFlowAsMono(request).block();
    }

    /**
     * Creates a quick payment with decoupled flow.
     *
     * @param request   the {@link QuickPaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateQuickPaymentResponse}
     */
    public CreateQuickPaymentResponse createQuickPaymentWithDecoupledFlow(QuickPaymentRequest request,
                                                                          final String requestId) {
        return createQuickPaymentWithDecoupledFlowAsMono(request, requestId).block();
    }

    /**
     * Creates a quick payment with decoupled flow.
     *
     * @param request the {@link QuickPaymentRequest}
     * @return the {@link CreateQuickPaymentResponse} {@link Mono}
     */
    public Mono<CreateQuickPaymentResponse> createQuickPaymentWithDecoupledFlowAsMono(QuickPaymentRequest request) {
        return quickPaymentsApiClient.createQuickPaymentWithDecoupledFlow(request);
    }

    /**
     * Creates a quick payment with decoupled flow.
     *
     * @param request   the {@link QuickPaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateQuickPaymentResponse} {@link Mono}
     */
    public Mono<CreateQuickPaymentResponse> createQuickPaymentWithDecoupledFlowAsMono(QuickPaymentRequest request,
                                                                                      final String requestId) {
        return quickPaymentsApiClient.createQuickPaymentWithDecoupledFlow(request, requestId);
    }

    /**
     * Creates a quick payment with gateway flow.
     *
     * @param request the {@link QuickPaymentRequest}
     * @return the {@link CreateQuickPaymentResponse}
     */
    public CreateQuickPaymentResponse createQuickPaymentWithGatewayFlow(QuickPaymentRequest request) {
        return createQuickPaymentWithGatewayFlowAsMono(request).block();
    }

    /**
     * Creates a quick payment with gateway flow.
     *
     * @param request   the {@link QuickPaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateQuickPaymentResponse}
     */
    public CreateQuickPaymentResponse createQuickPaymentWithGatewayFlow(QuickPaymentRequest request,
                                                                        final String requestId) {
        return createQuickPaymentWithGatewayFlowAsMono(request, requestId).block();
    }

    /**
     * Creates a quick payment with gateway flow.
     *
     * @param request the {@link QuickPaymentRequest}
     * @return the {@link CreateQuickPaymentResponse} {@link Mono}
     */
    public Mono<CreateQuickPaymentResponse> createQuickPaymentWithGatewayFlowAsMono(QuickPaymentRequest request) {
        return quickPaymentsApiClient.createQuickPaymentWithGatewayFlow(request);
    }

    /**
     * Creates a quick payment with gateway flow.
     *
     * @param request   the {@link QuickPaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateQuickPaymentResponse} {@link Mono}
     */
    public Mono<CreateQuickPaymentResponse> createQuickPaymentWithGatewayFlowAsMono(QuickPaymentRequest request,
                                                                                    final String requestId) {
        return quickPaymentsApiClient.createQuickPaymentWithGatewayFlow(request, requestId);
    }

    /**
     * Retrieves an existing quick payment by ID.
     *
     * @param quickPaymentId the quick payment ID
     * @return the {@link QuickPaymentResponse}
     */
    public QuickPaymentResponse getQuickPayment(UUID quickPaymentId) {
        return getQuickPaymentAsMono(quickPaymentId).block();
    }

    /**
     * Retrieves an existing quick payment by ID.
     *
     * @param quickPaymentId the quick payment ID
     * @param requestId      the optional correlation ID
     * @return the {@link QuickPaymentResponse}
     */
    public QuickPaymentResponse getQuickPayment(UUID quickPaymentId, String requestId) {
        return getQuickPaymentAsMono(quickPaymentId, requestId).block();
    }

    /**
     * Retrieves an existing quick payment by ID.
     *
     * @param quickPaymentId the quick payment ID
     * @return the {@link QuickPaymentResponse} {@link Mono}
     */
    public Mono<QuickPaymentResponse> getQuickPaymentAsMono(UUID quickPaymentId) {
        return quickPaymentsApiClient.getQuickPayment(quickPaymentId);
    }

    /**
     * Retrieves an existing quick payment by ID.
     *
     * @param quickPaymentId the quick payment ID
     * @param requestId      the optional correlation ID
     * @return the {@link QuickPaymentResponse} {@link Mono}
     */
    public Mono<QuickPaymentResponse> getQuickPaymentAsMono(UUID quickPaymentId, String requestId) {
        return quickPaymentsApiClient.getQuickPayment(quickPaymentId, requestId);
    }

    /**
     * Revokes an existing quick payment by ID.
     *
     * @param quickPaymentId the quick payment ID
     */
    public void revokeQuickPayment(UUID quickPaymentId) {
        revokeQuickPaymentAsMono(quickPaymentId).block();
    }

    /**
     * Revokes an existing quick payment by ID.
     *
     * @param consentId the quick payment ID
     * @param requestId the optional correlation ID
     */
    public void revokeQuickPayment(UUID consentId, final String requestId) {
        revokeQuickPaymentAsMono(consentId, requestId).block();
    }

    /**
     * Revokes an existing quick payment by ID.
     *
     * @param quickPaymentId the quick payment ID
     */
    public Mono<Void> revokeQuickPaymentAsMono(UUID quickPaymentId) {
        return quickPaymentsApiClient.revokeQuickPayment(quickPaymentId);
    }

    /**
     * Revokes an existing quick payment by ID.
     *
     * @param quickPaymentId the quick payment ID
     * @param requestId      the optional correlation ID
     */
    public Mono<Void> revokeQuickPaymentAsMono(UUID quickPaymentId, final String requestId) {
        return quickPaymentsApiClient.revokeQuickPayment(quickPaymentId, requestId);
    }

    /**
     * Creates a single payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createSinglePayment(PaymentRequest request) {
        return createSinglePaymentAsMono(request).block();
    }

    /**
     * Creates a single payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createSinglePayment(PaymentRequest request, final String requestId) {
        return createSinglePaymentAsMono(request, requestId).block();
    }

    /**
     * Creates a single payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createSinglePaymentAsMono(PaymentRequest request) {
        return paymentsApiClient.createSinglePayment(request);
    }

    /**
     * Creates a single payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createSinglePaymentAsMono(PaymentRequest request, final String requestId) {
        return paymentsApiClient.createSinglePayment(request, requestId);
    }

    /**
     * Creates an enduring payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createEnduringPayment(PaymentRequest request) {
        return createEnduringPaymentAsMono(request).block();
    }

    /**
     * Creates an enduring payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createEnduringPayment(PaymentRequest request, final String requestId) {
        return createEnduringPaymentAsMono(request, requestId).block();
    }

    /**
     * Creates an enduring payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createEnduringPaymentAsMono(PaymentRequest request) {
        return paymentsApiClient.createEnduringPayment(request);
    }

    /**
     * Creates an enduring payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createEnduringPaymentAsMono(PaymentRequest request, final String requestId) {
        return paymentsApiClient.createEnduringPayment(request, requestId);
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createSinglePayment(PaymentRequest)}.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createWestpacPayment(PaymentRequest request) {
        return createWestpacPaymentAsMono(request).block();
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createSinglePayment(PaymentRequest, String)}.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createWestpacPayment(PaymentRequest request, final String requestId) {
        return createWestpacPaymentAsMono(request, requestId).block();
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createSinglePayment(PaymentRequest)}.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createWestpacPaymentAsMono(PaymentRequest request) {
        return paymentsApiClient.createWestpacPayment(request);
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createSinglePayment(PaymentRequest, String)}.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createWestpacPaymentAsMono(PaymentRequest request, final String requestId) {
        return paymentsApiClient.createWestpacPayment(request, requestId);
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param paymentId the payment ID
     * @return the {@link Payment}
     */
    public Payment getPayment(UUID paymentId) {
        return getPaymentAsMono(paymentId).block();
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param paymentId the payment ID
     * @param requestId the optional correlation ID
     * @return the {@link Payment}
     */
    public Payment getPayment(UUID paymentId, final String requestId) {
        return getPaymentAsMono(paymentId, requestId).block();
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param paymentId the payment ID
     * @return the {@link Payment} {@link Mono}
     */
    public Mono<Payment> getPaymentAsMono(UUID paymentId) {
        return paymentsApiClient.getPayment(paymentId);
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param paymentId the payment ID
     * @param requestId the optional correlation ID
     * @return the {@link Payment} {@link Mono}
     */
    public Mono<Payment> getPaymentAsMono(UUID paymentId, final String requestId) {
        return paymentsApiClient.getPayment(paymentId, requestId);
    }

    /**
     * Creates a full refund.
     *
     * @param request the {@link FullRefundRequest}
     * @return the {@link RefundResponse}
     */
    public RefundResponse createFullRefund(FullRefundRequest request) {
        return createFullRefundAsMono(request).block();
    }

    /**
     * Creates a full refund.
     *
     * @param request   the {@link FullRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse}
     */
    public RefundResponse createFullRefund(FullRefundRequest request, final String requestId) {
        return createFullRefundAsMono(request, requestId).block();
    }

    /**
     * Creates a full refund.
     *
     * @param request the {@link FullRefundRequest}
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createFullRefundAsMono(FullRefundRequest request) {
        return refundsApiClient.createFullRefund(request);
    }

    /**
     * Creates a full refund.
     *
     * @param request   the {@link FullRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createFullRefundAsMono(FullRefundRequest request, final String requestId) {
        return refundsApiClient.createFullRefund(request, requestId);
    }

    /**
     * Creates a partial refund.
     *
     * @param request the {@link PartialRefundRequest}
     * @return the {@link RefundResponse}
     */
    public RefundResponse createPartialRefund(PartialRefundRequest request) {
        return createPartialRefundAsMono(request).block();
    }

    /**
     * Creates a partial refund.
     *
     * @param request   the {@link PartialRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse}
     */
    public RefundResponse createPartialRefund(PartialRefundRequest request, final String requestId) {
        return createPartialRefundAsMono(request, requestId).block();
    }

    /**
     * Creates a partial refund.
     *
     * @param request the {@link PartialRefundRequest}
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createPartialRefundAsMono(PartialRefundRequest request) {
        return refundsApiClient.createPartialRefund(request);
    }

    /**
     * Creates a partial refund.
     *
     * @param request   the {@link PartialRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createPartialRefundAsMono(PartialRefundRequest request, final String requestId) {
        return refundsApiClient.createPartialRefund(request, requestId);
    }

    /**
     * Creates an account number refund.
     *
     * @param request the {@link AccountNumberRefundRequest}
     * @return the {@link RefundResponse}
     */
    public RefundResponse createAccountNumberRefund(AccountNumberRefundRequest request) {
        return createAccountNumberRefundAsMono(request).block();
    }

    /**
     * Creates an account number refund.
     *
     * @param request   the {@link AccountNumberRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse}
     */
    public RefundResponse createAccountNumberRefund(AccountNumberRefundRequest request, final String requestId) {
        return createAccountNumberRefundAsMono(request, requestId).block();
    }

    /**
     * Creates an account number refund.
     *
     * @param request the {@link AccountNumberRefundRequest}
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createAccountNumberRefundAsMono(AccountNumberRefundRequest request) {
        return refundsApiClient.createAccountNumberRefund(request);
    }

    /**
     * Creates an account number refund.
     *
     * @param request   the {@link AccountNumberRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createAccountNumberRefundAsMono(AccountNumberRefundRequest request,
                                                                final String requestId) {
        return refundsApiClient.createAccountNumberRefund(request, requestId);
    }

    /**
     * Retrieves an existing refund by ID.
     *
     * @param refundId the refund ID
     * @return the {@link Payment}
     */
    public Refund getRefund(UUID refundId) {
        return getRefundAsMono(refundId).block();
    }

    /**
     * Retrieves an existing refund by ID.
     *
     * @param refundId  the refund ID
     * @param requestId the optional correlation ID
     * @return the {@link Payment}
     */
    public Refund getRefund(UUID refundId, final String requestId) {
        return getRefundAsMono(refundId, requestId).block();
    }

    /**
     * Retrieves an existing refund by ID.
     *
     * @param refundId the refund ID
     * @return the {@link Payment} {@link Mono}
     */
    public Mono<Refund> getRefundAsMono(UUID refundId) {
        return refundsApiClient.getRefund(refundId);
    }

    /**
     * Retrieves an existing refund by ID.
     *
     * @param refundId  the refund ID
     * @param requestId the optional correlation ID
     * @return the {@link Payment} {@link Mono}
     */
    public Mono<Refund> getRefundAsMono(UUID refundId, final String requestId) {
        return refundsApiClient.getRefund(refundId, requestId);
    }
}
