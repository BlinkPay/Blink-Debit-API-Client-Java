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
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Period;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

import static nz.co.blink.debit.IntegrationTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link EnduringConsentsApiClient}.
 * Tests enduring consent lifecycle for recurring payments.
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnduringConsentsApiClientIntegrationTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

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
    @DisplayName("Verify that enduring consent with redirect flow is created")
    @Order(1)
    void createEnduringConsentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        EnduringConsentRequest request = new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(DEFAULT_BANK)
                                .redirectUri(URI.create(REDIRECT_URI))))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .maximumAmountPayment(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .period(Period.FORTNIGHTLY)
                .fromTimestamp(OffsetDateTime.now(ZONE_ID))
                .hashedCustomerIdentifier(CUSTOMER_HASH);

        CreateConsentResponse response = client.getEnduringConsentsApi().createEnduringConsent(request);

        assertThat(response).isNotNull();
        consentId = response.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(response.getRedirectUri()).isNotNull();
        assertThat(response.getRedirectUri().toString()).contains("authorize");
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is retrieved")
    @Order(2)
    void getEnduringConsentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");
        assumeTrue(consentId != null, "Consent not created in previous test");

        Consent consent = client.getEnduringConsentsApi().getConsent(consentId);

        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AWAITING_AUTHORISATION, Collections.emptyList());

        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getDetail()).isNotNull();
        assertThat(consent.getDetail()).isInstanceOf(EnduringConsentRequest.class);
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is revoked")
    @Order(3)
    void revokeEnduringConsentWithRedirectFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");
        assumeTrue(consentId != null, "Consent not created in previous test");

        assertThatNoException().isThrownBy(() -> client.getEnduringConsentsApi().revokeConsent(consentId));

        Consent consent = client.getEnduringConsentsApi().getConsent(consentId);

        assertThat(consent).isNotNull();
        assertThat(consent.getStatus()).isEqualTo(Consent.StatusEnum.REVOKED);
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow is created")
    @Order(4)
    void createEnduringConsentWithGatewayFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        EnduringConsentRequest request = new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(URI.create(REDIRECT_URI))))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .maximumAmountPayment(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .period(Period.FORTNIGHTLY)
                .fromTimestamp(OffsetDateTime.now(ZONE_ID))
                .hashedCustomerIdentifier(CUSTOMER_HASH);

        CreateConsentResponse response = client.getEnduringConsentsApi().createEnduringConsent(request);

        assertThat(response).isNotNull();
        UUID gatewayConsentId = response.getConsentId();
        assertThat(gatewayConsentId).isNotNull();

        // Gateway flow returns a gateway redirect URI
        assertThat(response.getRedirectUri())
                .isNotNull()
                .asString()
                .contains("/gateway/pay?id=" + gatewayConsentId);

        // Clean up - revoke the consent
        client.getEnduringConsentsApi().revokeConsent(gatewayConsentId);
    }

    @Test
    @DisplayName("Verify that getting non-existent enduring consent returns 404 error")
    @Order(5)
    void getNonExistentConsent() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> client.getEnduringConsentsApi().getConsent(nonExistentId))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageMatching(".*HTTP (403|404).*");
    }

    @Test
    @DisplayName("Verify that creating enduring consent with null request throws validation error")
    @Order(6)
    void createEnduringConsentWithNullRequest() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getEnduringConsentsApi().createEnduringConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that getting enduring consent with null ID throws validation error")
    @Order(7)
    void getEnduringConsentWithNullId() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getEnduringConsentsApi().getConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that revoking enduring consent with null ID throws validation error")
    @Order(8)
    void revokeEnduringConsentWithNullId() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThatThrownBy(() -> client.getEnduringConsentsApi().revokeConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Verify that enduring consent with decoupled flow is created")
    @Order(9)
    void createEnduringConsentWithDecoupledFlow() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        EnduringConsentRequest request = new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(DEFAULT_BANK)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue(PHONE_NUMBER)
                                .callbackUrl(URI.create(CALLBACK_URL))))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .maximumAmountPayment(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .period(Period.FORTNIGHTLY)
                .fromTimestamp(OffsetDateTime.now(ZONE_ID))
                .hashedCustomerIdentifier(CUSTOMER_HASH);

        CreateConsentResponse response = client.getEnduringConsentsApi().createEnduringConsent(request);

        assertThat(response).isNotNull();
        UUID decoupledConsentId = response.getConsentId();
        assertThat(decoupledConsentId).isNotNull();

        // Decoupled flow returns null redirect URI (user authorizes via their banking app)
        assertThat(response.getRedirectUri()).isNull();

        // Clean up - revoke the consent
        client.getEnduringConsentsApi().revokeConsent(decoupledConsentId);
    }
}
