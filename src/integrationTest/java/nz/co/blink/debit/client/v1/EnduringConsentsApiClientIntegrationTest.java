/**
 * Copyright (c) 2022 BlinkPay
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

import nz.co.blink.debit.config.BlinkDebitConfiguration;
import nz.co.blink.debit.dto.v1.AccessTokenResponse;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Period;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.RedirectFlowHint;
import nz.co.blink.debit.exception.ExpiredAccessTokenException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * The integration test for {@link EnduringConsentsApiClient}.
 */
@SpringBootTest(classes = {OAuthApiClient.class, EnduringConsentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@ActiveProfiles("test")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnduringConsentsApiClientIntegrationTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

    @Autowired
    private OAuthApiClient oAuthApiClient;

    @Autowired
    private EnduringConsentsApiClient client;

    private static String accessToken;

    private static UUID consentId;

    @BeforeEach
    void setUp() {
        if (StringUtils.isBlank(accessToken)) {
            Mono<AccessTokenResponse> accessTokenResponseMono = oAuthApiClient.generateAccessToken(UUID.randomUUID().toString());

            assertThat(accessTokenResponseMono).isNotNull();
            AccessTokenResponse accessTokenResponse = accessTokenResponseMono.block();
            assertThat(accessTokenResponse).isNotNull();

            accessToken = accessTokenResponse.getAccessToken();
            assertThat(accessToken)
                    .isNotBlank()
                    .startsWith("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjdiZDdGYlpSdC1yT1oxVTRBaEdCbCJ9");
        }
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is created in BNZ")
    @Order(1)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void createEnduringConsentWithRedirectFlowInBnz() throws ExpiredAccessTokenException {
        Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ, REDIRECT_URI, Period.FORTNIGHTLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual).isNotNull();

        consentId = actual.getConsentId();
        assertThat(consentId).isNotNull();

        assertThat(actual.getRedirectUri())
                .isNotBlank()
                .startsWith("https://secure.sandbox.bnz.co.nz/pingfederate/as/authorization.oauth2"
                        + "?scope=payments%20openid&response_type=code%20id_token")
                .contains("&request=", "&state=", "&nonce=")
                .endsWith("&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn"
                        + "&client_id=qVAnzBU96TemgkatLe8R16yn3Wm8KFoU");
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is retrieved from BNZ")
    @Order(2)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void getEnduringConsentWithRedirectFlowFromBnz() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId);

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is revoked in BNZ")
    @Order(3)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void revokeEnduringConsentWithRedirectFlowInBnz() throws ExpiredAccessTokenException {
        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId).block());

        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId);

        Consent actual = consentMono.block();
        assertThat(actual).isNotNull();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REVOKED, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that rejected/timed out enduring consent with redirect flow is retrieved from BNZ")
    @Order(4)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void getRejectedEnduringConsentWithRedirectFlowFromBnz() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                UUID.fromString("a3da5f1c-6fc2-43ad-a5eb-3cc538e2d98f"));

        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REJECTED, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and redirect flow hint is created in BNZ")
    @Order(5)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void createEnduringConsentWithGatewayFlowAndRedirectFlowHintInBnz() throws ExpiredAccessTokenException {
        Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, Bank.BNZ, REDIRECT_URI, Period.FORTNIGHTLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual).isNotNull();

        consentId = actual.getConsentId();
        assertThat(consentId).isNotNull();

        assertThat(actual.getRedirectUri())
                .isNotBlank()
                .isEqualTo("https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + consentId);
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and redirect flow hint is retrieved from BNZ")
    @Order(6)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void getEnduringConsentWithGatewayFlowAndRedirectFlowHintFromBnz() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId);

        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, "https://www.blinkpay.co.nz/sample-merchant-return-page");
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(RedirectFlowHint.class);
        RedirectFlowHint flowHint = (RedirectFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(RedirectFlowHint::getType, RedirectFlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.BNZ);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and redirect flow hint is revoked in BNZ")
    @Order(7)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void revokeEnduringConsentWithGatewayFlowAndRedirectFlowHintInBnz() throws ExpiredAccessTokenException {
        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId).block());

        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId);

        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REVOKED, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, "https://www.blinkpay.co.nz/sample-merchant-return-page");
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(RedirectFlowHint.class);
        RedirectFlowHint flowHint = (RedirectFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(RedirectFlowHint::getType, RedirectFlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.BNZ);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and decoupled flow hint is created in BNZ")
    @Order(8)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void createEnduringConsentWithGatewayFlowAndDecoupledFlowHintInBnz() throws ExpiredAccessTokenException {
        Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, Bank.BNZ, null, Period.FORTNIGHTLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.DECOUPLED, IdentifierType.CONSENT_ID,
                "f71900df-2528-4847-8692-f1db6864e4ae", null);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual).isNotNull();

        consentId = actual.getConsentId();
        assertThat(consentId).isNotNull();

        assertThat(actual.getRedirectUri())
                .isNotBlank()
                .isEqualTo("https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + consentId);
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and decoupled flow hint is retrieved from BNZ")
    @Order(9)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void getEnduringConsentWithGatewayFlowAndDecoupledFlowHintFromBnz() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId);

        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, "https://www.blinkpay.co.nz/sample-merchant-return-page");
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(DecoupledFlowHint.class);
        DecoupledFlowHint flowHint = (DecoupledFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(DecoupledFlowHint::getType, DecoupledFlowHint::getBank,
                        DecoupledFlowHint::getIdentifierType, DecoupledFlowHint::getIdentifierValue)
                .containsExactly(FlowHint.TypeEnum.DECOUPLED, Bank.BNZ,
                        IdentifierType.CONSENT_ID, "f71900df-2528-4847-8692-f1db6864e4ae");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and decoupled flow hint is revoked in BNZ")
    @Order(10)
    @Disabled("Re-enable when BNZ enduring consent is enabled")
    void revokeEnduringConsentWithGatewayFlowAndDecoupledFlowHintInBnz() throws ExpiredAccessTokenException {
        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId).block());

        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId);

        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REVOKED, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, "https://www.blinkpay.co.nz/sample-merchant-return-page");
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(DecoupledFlowHint.class);
        DecoupledFlowHint flowHint = (DecoupledFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(DecoupledFlowHint::getType, DecoupledFlowHint::getBank,
                        DecoupledFlowHint::getIdentifierType, DecoupledFlowHint::getIdentifierValue)
                .containsExactly(FlowHint.TypeEnum.DECOUPLED, Bank.BNZ,
                        IdentifierType.CONSENT_ID, "f71900df-2528-4847-8692-f1db6864e4ae");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with decoupled flow is created in PNZ")
    @Order(11)
    void createEnduringConsentWithDecoupledFlowInPnz() throws ExpiredAccessTokenException {
        Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.FORTNIGHTLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425", null
        );

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual).isNotNull();
        consentId = actual.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(actual.getRedirectUri()).isBlank();
    }

    @Test
    @DisplayName("Verify that enduring consent with decoupled flow is retrieved from PNZ")
    @Order(12)
    void getEnduringConsentWithDecoupledFlowFromPnz() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId);

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(DecoupledFlow.class);
        DecoupledFlow flow = (DecoupledFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(DecoupledFlow::getType, DecoupledFlow::getBank, DecoupledFlow::getIdentifierType,
                        DecoupledFlow::getIdentifierValue)
                .containsExactly(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER,
                        "+6449144425");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is revoked in PNZ")
    @Order(13)
    void revokeEnduringConsentWithDecoupledFlowInPnz() throws ExpiredAccessTokenException {
        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId).block());

        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                consentId);

        Consent actual = consentMono.block();
        assertThat(actual).isNotNull();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REVOKED, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(DecoupledFlow.class);
        DecoupledFlow flow = (DecoupledFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(DecoupledFlow::getType, DecoupledFlow::getBank, DecoupledFlow::getIdentifierType,
                        DecoupledFlow::getIdentifierValue)
                .containsExactly(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER,
                        "+6449144425");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that rejected/timed out enduring consent with decoupled flow is retrieved from PNZ")
    @Order(14)
    void getRejectedEnduringConsentWithDecoupledFlowFromPnz() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.randomUUID().toString(), accessToken,
                UUID.fromString("5c65366f-00db-4e7b-88ae-eabf18e072ed"));

        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REJECTED, null, Collections.emptySet());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, REDIRECT_URI);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNotNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }
}
