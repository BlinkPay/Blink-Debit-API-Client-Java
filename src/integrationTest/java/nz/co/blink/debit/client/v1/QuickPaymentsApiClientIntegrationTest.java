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
import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.RedirectFlowHint;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import nz.co.blink.debit.exception.ExpiredAccessTokenException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * The integration test for {@link QuickPaymentsApiClient}.
 */
@SpringBootTest(classes = {OAuthApiClient.class, QuickPaymentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@ActiveProfiles("test")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuickPaymentsApiClientIntegrationTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    @Autowired
    private OAuthApiClient oAuthApiClient;

    @Autowired
    private QuickPaymentsApiClient client;

    private static String accessToken;

    private static UUID quickPaymentId;

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
    @DisplayName("Verify that quick payment with redirect flow is created in BNZ")
    @Order(1)
    void createQuickPaymentWithRedirectFlowInBnz() throws ExpiredAccessTokenException {
        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono = client.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ, REDIRECT_URI, "particulars", "code",
                "reference", "1.25", null);

        assertThat(createQuickPaymentResponseResponseMono).isNotNull();
        CreateQuickPaymentResponse actual = createQuickPaymentResponseResponseMono.block();
        assertThat(actual).isNotNull();

        quickPaymentId = actual.getQuickPaymentId();
        assertThat(quickPaymentId).isNotNull();

        assertThat(actual.getRedirectUri())
                .isNotBlank()
                .startsWith("https://secure.sandbox.bnz.co.nz/pingfederate/as/authorization.oauth2"
                        + "?scope=payments%20openid&response_type=code%20id_token")
                .contains("&request=", "&state=", "&nonce=")
                .endsWith("&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn"
                        + "&client_id=qVAnzBU96TemgkatLe8R16yn3Wm8KFoU");
    }

    @Test
    @DisplayName("Verify that quick payment with redirect flow is retrieved from BNZ")
    @Order(2)
    void getQuickPaymentWithRedirectFlowFromBnz() throws ExpiredAccessTokenException {
        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId);

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ, REDIRECT_URI);
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
    }

    @Test
    @DisplayName("Verify that quick payment with redirect flow is revoked in BNZ")
    @Order(3)
    void revokeQuickPaymentWithRedirectFlowInBnz() throws ExpiredAccessTokenException {
        assertThatNoException().isThrownBy(() -> client.revokeQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId).block());

        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId);

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REVOKED, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ, REDIRECT_URI);
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
    }

    @Test
    @DisplayName("Verify that rejected/timed out quick payment with redirect flow is retrieved from BNZ")
    @Order(4)
    void getRejectedQuickPaymentWithRedirectFlowFromBnz() throws ExpiredAccessTokenException {
        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                UUID.fromString("c302f96c-9e27-49b0-a734-01252fa0fd93"));

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(UUID.fromString("c302f96c-9e27-49b0-a734-01252fa0fd93"));
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REJECTED, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ, REDIRECT_URI);
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.50");
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is created in BNZ")
    @Order(5)
    void createQuickPaymentWithGatewayFlowAndRedirectFlowHintInBnz() throws ExpiredAccessTokenException {
        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono = client.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, Bank.BNZ, REDIRECT_URI, "particulars", "code",
                "reference", "1.25", FlowHint.TypeEnum.REDIRECT);

        assertThat(createQuickPaymentResponseResponseMono).isNotNull();
        CreateQuickPaymentResponse actual = createQuickPaymentResponseResponseMono.block();
        assertThat(actual).isNotNull();

        quickPaymentId = actual.getQuickPaymentId();
        assertThat(quickPaymentId).isNotNull();

        assertThat(actual.getRedirectUri())
                .isNotBlank()
                .isEqualTo("https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + quickPaymentId);
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is retrieved from BNZ")
    @Order(6)
    void getQuickPaymentWithGatewayFlowAndRedirectFlowHintFromBnz() throws ExpiredAccessTokenException {
        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId);

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(RedirectFlowHint.class);
        RedirectFlowHint flowHint = (RedirectFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(RedirectFlowHint::getType, RedirectFlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.BNZ);
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is revoked in BNZ")
    @Order(7)
    void revokeQuickPaymentWithGatewayFlowAndRedirectFlowHintInBnz() throws ExpiredAccessTokenException {
        assertThatNoException().isThrownBy(() -> client.revokeQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId).block());

        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId);

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REVOKED, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(RedirectFlowHint.class);
        RedirectFlowHint flowHint = (RedirectFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(RedirectFlowHint::getType, RedirectFlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.BNZ);
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and decoupled flow hint is created in BNZ")
    @Order(8)
    void createQuickPaymentWithGatewayFlowAndDecoupledFlowHintInBnz() throws ExpiredAccessTokenException {
        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono = client.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, Bank.BNZ, REDIRECT_URI, "particulars", "code",
                "reference", "1.25", FlowHint.TypeEnum.DECOUPLED, IdentifierType.CONSENT_ID,
                "f71900df-2528-4847-8692-f1db6864e4ae", null);

        assertThat(createQuickPaymentResponseResponseMono).isNotNull();
        CreateQuickPaymentResponse actual = createQuickPaymentResponseResponseMono.block();
        assertThat(actual).isNotNull();

        quickPaymentId = actual.getQuickPaymentId();
        assertThat(quickPaymentId).isNotNull();

        assertThat(actual.getRedirectUri())
                .isNotBlank()
                .isEqualTo("https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + quickPaymentId);
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and decoupled flow hint is retrieved from BNZ")
    @Order(9)
    void getQuickPaymentWithGatewayFlowAndDecoupledFlowHintFromBnz() throws ExpiredAccessTokenException {
        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId);

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
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
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and decoupled flow hint is revoked in BNZ")
    @Order(10)
    void revokeQuickPaymentWithGatewayFlowAndDecoupledFlowHintInBnz() throws ExpiredAccessTokenException {
        assertThatNoException().isThrownBy(() -> client.revokeQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId).block());

        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId);

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REVOKED, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
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
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is created in Westpac")
    @Order(11)
    void createQuickPaymentWithGatewayFlowAndRedirectFlowHintInWestpac() throws ExpiredAccessTokenException {
        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono = client.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, Bank.WESTPAC, REDIRECT_URI, "particulars", "code",
                "reference", "1.25", FlowHint.TypeEnum.REDIRECT);

        assertThat(createQuickPaymentResponseResponseMono).isNotNull();
        CreateQuickPaymentResponse actual = createQuickPaymentResponseResponseMono.block();
        assertThat(actual).isNotNull();

        quickPaymentId = actual.getQuickPaymentId();
        assertThat(quickPaymentId).isNotNull();

        assertThat(actual.getRedirectUri())
                .isNotBlank()
                .isEqualTo("https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + quickPaymentId);
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is retrieved from Westpac")
    @Order(12)
    void getQuickPaymentWithGatewayFlowAndRedirectFlowHintFromWestpac() throws ExpiredAccessTokenException {
        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId);

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(RedirectFlowHint.class);
        RedirectFlowHint flowHint = (RedirectFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(RedirectFlowHint::getType, RedirectFlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.WESTPAC);
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is revoked in Westpac")
    @Order(13)
    void revokeQuickPaymentWithGatewayFlowAndRedirectFlowHintInWestpac() throws ExpiredAccessTokenException {
        assertThatNoException().isThrownBy(() -> client.revokeQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId).block());

        Mono<QuickPaymentResponse> consentMono = client.getQuickPayment(UUID.randomUUID().toString(), accessToken,
                quickPaymentId);

        QuickPaymentResponse actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.REVOKED, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isNotNull();
        assertThat(consent.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(consent.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) consent.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(RedirectFlowHint.class);
        RedirectFlowHint flowHint = (RedirectFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(RedirectFlowHint::getType, RedirectFlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.WESTPAC);
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
    }
}
