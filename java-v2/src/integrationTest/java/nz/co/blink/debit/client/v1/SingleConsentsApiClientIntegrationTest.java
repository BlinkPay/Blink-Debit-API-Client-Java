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

import nz.co.blink.debit.IntegrationTestConstants;
import nz.co.blink.debit.config.BlinkDebitConfig;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.AuthFlow;

import java.net.URI;
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
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

import java.util.Collections;
import java.util.UUID;

import static nz.co.blink.debit.IntegrationTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link SingleConsentsApiClient}.
 * Tests the full lifecycle: create, retrieve, revoke.
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SingleConsentsApiClientIntegrationTest {

    private static BlinkDebitClient client;
    private static boolean credentialsAvailable;
    private static UUID consentId;

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
    @DisplayName("Verify that single consent with redirect flow is created")
    @Order(1)
    void createSingleConsentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        SingleConsentRequest request = new SingleConsentRequest()
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

        CreateConsentResponse response = client.getSingleConsentsApi().createSingleConsent(request);

        assertThat(response).isNotNull();
        consentId = response.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(response.getRedirectUri()).isNotNull();
        assertThat(response.getRedirectUri().toString()).contains("authorize");
    }

    @Test
    @DisplayName("Verify that single consent with redirect flow is retrieved")
    @Order(2)
    void getSingleConsentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");
        assumeTrue(consentId != null, "Consent not created in previous test");

        Consent consent = client.getSingleConsentsApi().getConsent(consentId);

        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AWAITING_AUTHORISATION, Collections.emptyList());

        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getDetail()).isNotNull();
        assertThat(consent.getDetail()).isInstanceOf(SingleConsentRequest.class);
    }

    @Test
    @DisplayName("Verify that single consent with redirect flow is revoked")
    @Order(3)
    void revokeSingleConsentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");
        assumeTrue(consentId != null, "Consent not created in previous test");

        assertThatNoException().isThrownBy(() -> client.getSingleConsentsApi().revokeConsent(consentId));

        Consent consent = client.getSingleConsentsApi().getConsent(consentId);

        assertThat(consent).isNotNull();
        assertThat(consent.getStatus()).isEqualTo(Consent.StatusEnum.REVOKED);
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow is created")
    @Order(4)
    void createSingleConsentWithGatewayFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        SingleConsentRequest request = new SingleConsentRequest()
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

        CreateConsentResponse response = client.getSingleConsentsApi().createSingleConsent(request);

        assertThat(response).isNotNull();
        UUID gatewayConsentId = response.getConsentId();
        assertThat(gatewayConsentId).isNotNull();

        // Gateway flow returns a gateway redirect URI, not a direct bank authorization URI
        assertThat(response.getRedirectUri())
                .isNotNull()
                .asString()
                .contains("/gateway/pay?id=" + gatewayConsentId);

        // Clean up - revoke the consent
        client.getSingleConsentsApi().revokeConsent(gatewayConsentId);
    }

    @Test
    @DisplayName("Verify that getting non-existent consent returns 404 error")
    @Order(5)
    void getNonExistentConsent() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> client.getSingleConsentsApi().getConsent(nonExistentId))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageMatching(".*HTTP (403|404).*");
    }

    @Test
    @DisplayName("Verify that creating consent with null request throws validation error")
    @Order(6)
    void createConsentWithNullRequest() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getSingleConsentsApi().createSingleConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that getting consent with null ID throws validation error")
    @Order(7)
    void getConsentWithNullId() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getSingleConsentsApi().getConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that revoking consent with null ID throws validation error")
    @Order(8)
    void revokeConsentWithNullId() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getSingleConsentsApi().revokeConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that single consent with decoupled flow is created")
    @Order(9)
    void createSingleConsentWithDecoupledFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        SingleConsentRequest request = new SingleConsentRequest()
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

        CreateConsentResponse response = client.getSingleConsentsApi().createSingleConsent(request);

        assertThat(response).isNotNull();
        UUID decoupledConsentId = response.getConsentId();
        assertThat(decoupledConsentId).isNotNull();

        // Decoupled flow returns null redirect URI (user authorizes via their banking app)
        assertThat(response.getRedirectUri()).isNull();

        // Clean up - revoke the consent
        client.getSingleConsentsApi().revokeConsent(decoupledConsentId);
    }
}
