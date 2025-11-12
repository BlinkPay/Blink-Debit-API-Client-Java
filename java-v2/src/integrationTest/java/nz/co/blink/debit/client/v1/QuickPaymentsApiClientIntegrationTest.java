/**
 * Copyright (c) 2025 BlinkPay
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.config.BlinkDebitConfig;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.AuthFlow;
import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.QuickPaymentRequest;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.util.UUID;

import static nz.co.blink.debit.IntegrationTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link QuickPaymentsApiClient}.
 * Tests quick payment lifecycle (consent + payment in one step).
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuickPaymentsApiClientIntegrationTest {

    private static BlinkDebitClient client;
    private static boolean credentialsAvailable;
    private static UUID quickPaymentId;

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

    @Test
    @DisplayName("Verify that quick payment with redirect flow is created")
    @Order(1)
    void createQuickPaymentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(DEFAULT_BANK)
                                .redirectUri(URI.create(REDIRECT_URI))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars(PARTICULARS)
                        .code(CODE)
                        .reference(REFERENCE))
                .hashedCustomerIdentifier(CUSTOMER_HASH);

        CreateQuickPaymentResponse response = client.getQuickPaymentsApi().createQuickPayment(request);

        assertThat(response).isNotNull();
        quickPaymentId = response.getQuickPaymentId();
        assertThat(quickPaymentId).isNotNull();
        assertThat(response.getRedirectUri()).isNotNull();
        assertThat(response.getRedirectUri().toString()).contains("authorize");
    }

    @Test
    @DisplayName("Verify that quick payment with redirect flow is retrieved")
    @Order(2)
    void getQuickPaymentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");
        assumeTrue(quickPaymentId != null, "Quick payment not created in previous test");

        QuickPaymentResponse response = client.getQuickPaymentsApi().getQuickPayment(quickPaymentId);

        assertThat(response).isNotNull();
        assertThat(response.getQuickPaymentId()).isEqualTo(quickPaymentId);
        assertThat(response.getConsent()).isNotNull();
    }

    @Test
    @DisplayName("Verify that quick payment with redirect flow is revoked")
    @Order(3)
    void revokeQuickPaymentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");
        assumeTrue(quickPaymentId != null, "Quick payment not created in previous test");

        assertThatNoException().isThrownBy(() -> client.getQuickPaymentsApi().revokeQuickPayment(quickPaymentId));

        QuickPaymentResponse response = client.getQuickPaymentsApi().getQuickPayment(quickPaymentId);

        assertThat(response).isNotNull();
        assertThat(response.getConsent()).isNotNull();
        assertThat(response.getConsent().getStatus()).isIn(
                nz.co.blink.debit.dto.v1.Consent.StatusEnum.REVOKED,
                nz.co.blink.debit.dto.v1.Consent.StatusEnum.REJECTED
        );
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow is created")
    @Order(4)
    void createQuickPaymentWithGatewayFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(URI.create(REDIRECT_URI))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars(PARTICULARS)
                        .code(CODE)
                        .reference(REFERENCE))
                .hashedCustomerIdentifier(CUSTOMER_HASH);

        CreateQuickPaymentResponse response = client.getQuickPaymentsApi().createQuickPayment(request);

        assertThat(response).isNotNull();
        UUID gatewayPaymentId = response.getQuickPaymentId();
        assertThat(gatewayPaymentId).isNotNull();

        // Gateway flow returns a gateway redirect URI
        assertThat(response.getRedirectUri())
                .isNotNull()
                .asString()
                .contains("/gateway/pay?id=" + gatewayPaymentId);

        // Clean up - revoke the quick payment
        client.getQuickPaymentsApi().revokeQuickPayment(gatewayPaymentId);
    }

    @Test
    @DisplayName("Verify that getting non-existent quick payment returns 404 error")
    @Order(5)
    void getNonExistentQuickPayment() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> client.getQuickPaymentsApi().getQuickPayment(nonExistentId))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageMatching(".*HTTP (403|404).*");
    }

    @Test
    @DisplayName("Verify that creating quick payment with null request throws validation error")
    @Order(6)
    void createQuickPaymentWithNullRequest() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getQuickPaymentsApi().createQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that getting quick payment with null ID throws validation error")
    @Order(7)
    void getQuickPaymentWithNullId() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getQuickPaymentsApi().getQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that revoking quick payment with null ID throws validation error")
    @Order(8)
    void revokeQuickPaymentWithNullId() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getQuickPaymentsApi().revokeQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that quick payment with decoupled flow is created")
    @Order(9)
    void createQuickPaymentWithDecoupledFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(DEFAULT_BANK)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue(PHONE_NUMBER)
                                .callbackUrl(URI.create(CALLBACK_URL))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars(PARTICULARS)
                        .code(CODE)
                        .reference(REFERENCE))
                .hashedCustomerIdentifier(CUSTOMER_HASH);

        CreateQuickPaymentResponse response = client.getQuickPaymentsApi().createQuickPayment(request);

        assertThat(response).isNotNull();
        UUID decoupledPaymentId = response.getQuickPaymentId();
        assertThat(decoupledPaymentId).isNotNull();

        // Decoupled flow returns null redirect URI (user authorizes via their banking app)
        assertThat(response.getRedirectUri()).isNull();

        // Clean up - revoke the quick payment
        client.getQuickPaymentsApi().revokeQuickPayment(decoupledPaymentId);
    }
}
