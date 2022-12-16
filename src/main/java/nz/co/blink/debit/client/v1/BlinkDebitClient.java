package nz.co.blink.debit.client.v1;

import io.netty.handler.logging.LogLevel;
import nz.co.blink.debit.dto.v1.BankMetadata;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.QuickPaymentRequest;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.dto.v1.Refund;
import nz.co.blink.debit.dto.v1.RefundDetail;
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

import javax.validation.Validator;
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

    private final Validator validator;

    /**
     * Default constructor for Spring-based consumer.
     *
     * @param singleConsentsApiClient   the {@link SingleConsentsApiClient}
     * @param enduringConsentsApiClient the {@link EnduringConsentsApiClient}
     * @param quickPaymentsApiClient    the {@link QuickPaymentsApiClient}
     * @param paymentsApiClient         the {@link PaymentsApiClient}
     * @param refundsApiClient          the {@link RefundsApiClient}
     * @param metaApiClient             the {@link MetaApiClient}
     * @param validator                 the {@link Validator}
     */
    @Autowired
    public BlinkDebitClient(SingleConsentsApiClient singleConsentsApiClient,
                            EnduringConsentsApiClient enduringConsentsApiClient,
                            QuickPaymentsApiClient quickPaymentsApiClient, PaymentsApiClient paymentsApiClient,
                            RefundsApiClient refundsApiClient, MetaApiClient metaApiClient, Validator validator) {
        this.singleConsentsApiClient = singleConsentsApiClient;
        this.enduringConsentsApiClient = enduringConsentsApiClient;
        this.quickPaymentsApiClient = quickPaymentsApiClient;
        this.paymentsApiClient = paymentsApiClient;
        this.refundsApiClient = refundsApiClient;
        this.metaApiClient = metaApiClient;
        this.validator = validator;
    }

    /**
     * Constructor for pure Java application.
     *
     * @param properties the {@link Properties} retrieved from
     * @param validator  the {@link Validator}
     */
    public BlinkDebitClient(Properties properties, Validator validator) {
        this.validator = validator;
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
        singleConsentsApiClient = new SingleConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, validator);
        enduringConsentsApiClient = new EnduringConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, this.validator);
        quickPaymentsApiClient = new QuickPaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, validator);
        paymentsApiClient = new PaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, validator);
        refundsApiClient = new RefundsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, validator);
        metaApiClient = new MetaApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
    }

    /**
     * Constructor for pure Java application.
     *
     * @param debitUrl      the Blink Debit URL
     * @param clientId      the client ID
     * @param clientSecret  the client secret
     * @param activeProfile the active profile
     * @param validator     the {@link Validator}
     */
    public BlinkDebitClient(final String debitUrl, final String clientId, final String clientSecret,
                            final String activeProfile, Validator validator) {
        this.validator = validator;
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
        singleConsentsApiClient = new SingleConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, validator);
        enduringConsentsApiClient = new EnduringConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, this.validator);
        quickPaymentsApiClient = new QuickPaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, validator);
        paymentsApiClient = new PaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, validator);
        refundsApiClient = new RefundsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler, validator);
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
     * Creates a single consent.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createSingleConsent(SingleConsentRequest request) {
        return createSingleConsentAsMono(request).block();
    }

    /**
     * Creates a single consent.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createSingleConsent(SingleConsentRequest request,
                                                     final String requestId) {
        return createSingleConsentAsMono(request, requestId).block();
    }

    /**
     * Creates a single consent.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentAsMono(SingleConsentRequest request) {
        return singleConsentsApiClient.createSingleConsent(request);
    }

    /**
     * Creates a single consent with redirect flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentAsMono(SingleConsentRequest request,
                                                                 final String requestId) {
        return singleConsentsApiClient.createSingleConsent(request, requestId);
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
     * Creates an enduring consent.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createEnduringConsent(EnduringConsentRequest request) {
        return createEnduringConsentAsMono(request).block();
    }

    /**
     * Creates an enduring consent.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse}
     */
    public CreateConsentResponse createEnduringConsent(EnduringConsentRequest request,
                                                       final String requestId) {
        return createEnduringConsentAsMono(request, requestId).block();
    }

    /**
     * Creates an enduring consent.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsentAsMono(EnduringConsentRequest request) {
        return enduringConsentsApiClient.createEnduringConsent(request);
    }

    /**
     * Creates an enduring consent.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsentAsMono(EnduringConsentRequest request,
                                                                   final String requestId) {
        return enduringConsentsApiClient.createEnduringConsent(request, requestId);
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
     * Creates a quick payment.
     *
     * @param request the {@link QuickPaymentRequest}
     * @return the {@link CreateQuickPaymentResponse}
     */
    public CreateQuickPaymentResponse createQuickPayment(QuickPaymentRequest request) {
        return createQuickPaymentAsMono(request).block();
    }

    /**
     * Creates a quick payment.
     *
     * @param request   the {@link QuickPaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateQuickPaymentResponse}
     */
    public CreateQuickPaymentResponse createQuickPayment(QuickPaymentRequest request,
                                                         final String requestId) {
        return createQuickPaymentAsMono(request, requestId).block();
    }

    /**
     * Creates a quick payment.
     *
     * @param request the {@link QuickPaymentRequest}
     * @return the {@link CreateQuickPaymentResponse} {@link Mono}
     */
    public Mono<CreateQuickPaymentResponse> createQuickPaymentAsMono(QuickPaymentRequest request) {
        return quickPaymentsApiClient.createQuickPayment(request);
    }

    /**
     * Creates a quick payment.
     *
     * @param request   the {@link QuickPaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateQuickPaymentResponse} {@link Mono}
     */
    public Mono<CreateQuickPaymentResponse> createQuickPaymentAsMono(QuickPaymentRequest request,
                                                                     final String requestId) {
        return quickPaymentsApiClient.createQuickPayment(request, requestId);
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
     * Creates a payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createPayment(PaymentRequest request) {
        return createPaymentAsMono(request).block();
    }

    /**
     * Creates a payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createPayment(PaymentRequest request, final String requestId) {
        return createPaymentAsMono(request, requestId).block();
    }

    /**
     * Creates a payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPaymentAsMono(PaymentRequest request) {
        return paymentsApiClient.createPayment(request);
    }

    /**
     * Creates a payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPaymentAsMono(PaymentRequest request, final String requestId) {
        return paymentsApiClient.createPayment(request, requestId);
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createPayment(PaymentRequest)}.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse}
     */
    public PaymentResponse createWestpacPayment(PaymentRequest request) {
        return createWestpacPaymentAsMono(request).block();
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createPayment(PaymentRequest, String)}.
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
     * {@link #createPayment(PaymentRequest)}.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createWestpacPaymentAsMono(PaymentRequest request) {
        return paymentsApiClient.createWestpacPayment(request);
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createPayment(PaymentRequest, String)}.
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
     * Creates a refund.
     *
     * @param request the {@link RefundDetail}
     * @return the {@link RefundResponse}
     */
    public RefundResponse createRefund(RefundDetail request) {
        return createRefundAsMono(request).block();
    }

    /**
     * Creates a refund.
     *
     * @param request   the {@link RefundDetail}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse}
     */
    public RefundResponse createRefund(RefundDetail request, final String requestId) {
        return createRefundAsMono(request, requestId).block();
    }

    /**
     * Creates a refund.
     *
     * @param request the {@link RefundDetail}
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createRefundAsMono(RefundDetail request) {
        return refundsApiClient.createRefund(request);
    }

    /**
     * Creates a refund.
     *
     * @param request   the {@link RefundDetail}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createRefundAsMono(RefundDetail request, final String requestId) {
        return refundsApiClient.createRefund(request, requestId);
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
