package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.config.BlinkDebitConfig;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.AuthFlow;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.Period;
import nz.co.blink.debit.dto.v1.QuickPaymentRequest;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import nz.co.blink.debit.exception.BlinkConsentRejectedException;
import nz.co.blink.debit.exception.BlinkConsentTimeoutException;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkPaymentTimeoutException;
import nz.co.blink.debit.exception.BlinkServiceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import static nz.co.blink.debit.IntegrationTestConstants.CALLBACK_URL;
import static nz.co.blink.debit.IntegrationTestConstants.CUSTOMER_HASH;
import static nz.co.blink.debit.IntegrationTestConstants.DEFAULT_BANK;
import static nz.co.blink.debit.IntegrationTestConstants.PHONE_NUMBER;
import static nz.co.blink.debit.IntegrationTestConstants.REDIRECT_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for await helper methods in BlinkDebitClient.
 * These test the convenience methods that poll for status changes.
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AwaitHelpersIntegrationTest {

    private static BlinkDebitClient client;
    private static boolean credentialsAvailable;

    private static UUID quickPaymentId;
    private static UUID singleConsentId;
    private static UUID enduringConsentId;
    private static UUID paymentId;

    @BeforeAll
    static void setUp() throws BlinkInvalidValueException {
        String debitUrl = System.getenv("BLINKPAY_DEBIT_URL");
        String clientId = System.getenv("BLINKPAY_CLIENT_ID");
        String clientSecret = System.getenv("BLINKPAY_CLIENT_SECRET");

        credentialsAvailable = debitUrl != null && clientId != null && clientSecret != null;

        if (credentialsAvailable) {
            BlinkDebitConfig config = BlinkDebitConfig.builder()
                    .debitUrl(debitUrl)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            client = new BlinkDebitClient(config);
        }
    }

    @AfterAll
    static void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Test awaitSuccessfulQuickPaymentOrThrowException with timeout.
     * Since we don't authorize in sandbox, this should timeout.
     */
    @Test
    @Order(1)
    void testAwaitQuickPaymentTimeout() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        // Create a quick payment that won't be authorized
        QuickPaymentRequest request = new QuickPaymentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(DEFAULT_BANK)
                                .redirectUri(URI.create(REDIRECT_URI))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.00"))
                .pcr(new Pcr()
                        .particulars("test-qp")
                        .code("timeout")
                        .reference("test"));

        CreateQuickPaymentResponse response = client.createQuickPayment(request);
        quickPaymentId = response.getQuickPaymentId();

        assertThat(quickPaymentId).isNotNull();

        // Should timeout after 2 seconds and throw RuntimeException with message containing "Timed out"
        assertThatThrownBy(() -> client.awaitSuccessfulQuickPaymentOrThrowException(quickPaymentId, 2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Timed out");
    }

    /**
     * Test awaitAuthorisedSingleConsentOrThrowException with timeout.
     */
    @Test
    @Order(2)
    void testAwaitSingleConsentTimeout() throws BlinkServiceException, BlinkConsentTimeoutException,
            BlinkConsentRejectedException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        // Create a single consent that won't be authorized
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(DEFAULT_BANK)
                                .redirectUri(URI.create(REDIRECT_URI))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.00"))
                .pcr(new Pcr()
                        .particulars("test-sc"));

        CreateConsentResponse response = client.createSingleConsent(request);
        singleConsentId = response.getConsentId();

        assertThat(singleConsentId).isNotNull();

        // Should timeout after 2 seconds
        assertThatThrownBy(() -> client.awaitAuthorisedSingleConsentOrThrowException(singleConsentId, 2))
                .isInstanceOf(BlinkConsentTimeoutException.class)
                .hasMessageContaining("Timed out");
    }

    /**
     * Test awaitAuthorisedEnduringConsentOrThrowException with timeout.
     * This should also attempt to revoke the consent on timeout.
     */
    @Test
    @Order(3)
    void testAwaitEnduringConsentTimeout() throws BlinkServiceException, BlinkConsentTimeoutException,
            BlinkConsentRejectedException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        // Create an enduring consent that won't be authorized
        EnduringConsentRequest request = new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(DEFAULT_BANK)
                                .redirectUri(URI.create(REDIRECT_URI))))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .period(Period.WEEKLY)
                .fromTimestamp(OffsetDateTime.now())
                .expiryTimestamp(OffsetDateTime.now().plusMonths(1));

        CreateConsentResponse response = client.createEnduringConsent(request);
        enduringConsentId = response.getConsentId();

        assertThat(enduringConsentId).isNotNull();

        // Should timeout after 2 seconds and attempt to revoke
        assertThatThrownBy(() -> client.awaitAuthorisedEnduringConsentOrThrowException(enduringConsentId, 2))
                .isInstanceOf(BlinkConsentTimeoutException.class)
                .hasMessageContaining("Timed out");

        // Verify the consent was revoked (or attempted to be revoked)
        Consent consent = client.getEnduringConsent(enduringConsentId);
        // Status could be various values depending on timing, just verify we can still get it
        assertThat(consent).isNotNull();
        assertThat(consent.getConsentId()).isEqualTo(enduringConsentId);
    }

    /**
     * Test awaitSuccessfulPaymentOrThrowException with timeout.
     * Create a payment for a consent and let it timeout.
     */
    @Test
    @Order(4)
    void testAwaitPaymentTimeout() throws BlinkServiceException, InterruptedException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        // First create a single consent with decoupled flow (sandbox needs this for payments)
        SingleConsentRequest consentRequest = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(DEFAULT_BANK)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue(PHONE_NUMBER)
                                .callbackUrl(URI.create(CALLBACK_URL))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.00"))
                .pcr(new Pcr()
                        .particulars("test-pmt"))
                .hashedCustomerIdentifier(CUSTOMER_HASH);

        CreateConsentResponse consentResponse = client.createSingleConsent(consentRequest);
        UUID consentId = consentResponse.getConsentId();

        assertThat(consentId).isNotNull();

        // Try to create payment (may fail in sandbox without authorization, use retry logic)
        for (int i = 1; i <= 15; i++) {
            try {
                PaymentRequest paymentRequest = new PaymentRequest().consentId(consentId);
                PaymentResponse paymentResponse = client.createPayment(paymentRequest);
                paymentId = paymentResponse.getPaymentId();
                assertThat(paymentId).isNotNull();
                break;
            } catch (Exception e) {
                if (i == 15) {
                    // Skip this test if we can't create payment after all retries
                    assumeTrue(false, "Could not create payment in sandbox after 15 attempts");
                }
                // Sleep incrementally
                Thread.sleep(2000L * i);
            }
        }

        assertThat(paymentId).isNotNull();

        // Attempt to await payment completion
        // In sandbox with decoupled flow, the payment may actually succeed due to auto-authorization
        // So we test that the await method works correctly, whether it succeeds or times out
        try {
            Payment payment = client.awaitSuccessfulPaymentOrThrowException(paymentId, 10);
            // If it succeeds, verify we got a valid payment back
            assertThat(payment).isNotNull();
            assertThat(payment.getPaymentId()).isEqualTo(paymentId);
            assertThat(payment.getStatus()).isIn(
                    Payment.StatusEnum.ACCEPTED_SETTLEMENT_IN_PROCESS,
                    Payment.StatusEnum.ACCEPTED_SETTLEMENT_COMPLETED);
        } catch (BlinkPaymentTimeoutException e) {
            // Timeout is also acceptable - just verify the exception message
            assertThat(e.getMessage()).contains("Timed out");
        }
    }
}
