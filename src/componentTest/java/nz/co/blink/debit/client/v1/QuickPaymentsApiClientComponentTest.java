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
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
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
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * The component test case for {@link QuickPaymentsApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {OAuthApiClient.class, QuickPaymentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuickPaymentsApiClientComponentTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    @Autowired
    private OAuthApiClient oAuthApiClient;

    @Autowired
    private QuickPaymentsApiClient client;

    private static String accessToken;

    @BeforeEach
    void setUp() {
        if (StringUtils.isBlank(accessToken)) {
            ReflectionTestUtils.setField(oAuthApiClient, "webClient", WebClient.create("https://staging.debit.blinkpay.co.nz"));

            Mono<AccessTokenResponse> accessTokenResponseMono = oAuthApiClient.generateAccessToken(UUID.randomUUID().toString());

            assertThat(accessTokenResponseMono).isNotNull();
            AccessTokenResponse accessTokenResponse = accessTokenResponseMono.block();
            assertThat(accessTokenResponse).isNotNull();

            accessToken = accessTokenResponse.getAccessToken();
            assertThat(accessToken)
                    .isNotBlank()
                    .startsWith("ey");
        }
    }

    @Test
    @DisplayName("Verify that quick payment with redirect flow is created")
    @Order(1)
    void createQuickPaymentWithRedirectFlow() throws ExpiredAccessTokenException {
        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono = client.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ, REDIRECT_URI, "particulars", "code",
                "reference", "1.50", null);

        assertThat(createQuickPaymentResponseResponseMono).isNotNull();
        CreateQuickPaymentResponse actual = createQuickPaymentResponseResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateQuickPaymentResponse::getQuickPaymentId, CreateQuickPaymentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("2ea80e7a-ef5e-4431-8b47-cbae9c246414"),
                        "https://secure.sandbox.bnz.co.nz/pingfederate/as/authorization.oauth2?scope=payments%20openid&response_type=code%20id_token&request=header.payload.signature&state=2ea80e7a-ef5e-4431-8b47-cbae9c246414&nonce=7ba75d50-e754-4de9-a140-3095f4952671&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn&client_id=clientId");
    }

    @Test
    @DisplayName("Verify that quick payment with redirect flow is retrieved")
    @Order(2)
    void getQuickPaymentWithRedirectFlow() throws ExpiredAccessTokenException {
        UUID quickPaymentId = UUID.fromString("2ea80e7a-ef5e-4431-8b47-cbae9c246414");
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
                .containsExactly(Amount.CurrencyEnum.NZD, "1.50");
    }

    @Test
    @DisplayName("Verify that quick payment with redirect flow is revoked")
    @Order(3)
    void revokeQuickPaymentWithRedirectFlow() {
        assertThatNoException().isThrownBy(() -> client.revokeQuickPayment(UUID.randomUUID().toString(), accessToken,
                UUID.fromString("2ea80e7a-ef5e-4431-8b47-cbae9c246414")).block());
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is created")
    @Order(4)
    void createQuickPaymentWithGatewayFlow() throws ExpiredAccessTokenException {
        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono = client.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, Bank.WESTPAC, REDIRECT_URI, "particulars", "code",
                "reference", "1.50", FlowHint.TypeEnum.REDIRECT);

        assertThat(createQuickPaymentResponseResponseMono).isNotNull();
        CreateQuickPaymentResponse actual = createQuickPaymentResponseResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateQuickPaymentResponse::getQuickPaymentId, CreateQuickPaymentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("04157088-47ed-46ea-820e-6f726365b092"),
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=04157088-47ed-46ea-820e-6f726365b092");
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is retrieved")
    @Order(5)
    void getQuickPaymentWithGatewayFlow() throws ExpiredAccessTokenException {
        UUID quickPaymentId = UUID.fromString("04157088-47ed-46ea-820e-6f726365b092");
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
                .containsExactly(Amount.CurrencyEnum.NZD, "1.50");
    }

    @Test
    @DisplayName("Verify that quick payment with decoupled flow is created")
    @Order(6)
    void createQuickPaymentWithDecoupledFlow() throws ExpiredAccessTokenException {
        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono = client.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, "particulars", "code",
                "reference", "1.50", null, IdentifierType.PHONE_NUMBER, "+6449144425", "callbackUrl");

        assertThat(createQuickPaymentResponseResponseMono).isNotNull();
        CreateQuickPaymentResponse actual = createQuickPaymentResponseResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateQuickPaymentResponse::getQuickPaymentId, CreateQuickPaymentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("b9d04c2f-eea2-44cb-bf9f-72c834a3250b"), null);
    }

    @Test
    @DisplayName("Verify that quick payment with decoupled flow is retrieved")
    @Order(7)
    void getQuickPaymentWithDecoupledFlow() throws ExpiredAccessTokenException {
        UUID quickPaymentId = UUID.fromString("b9d04c2f-eea2-44cb-bf9f-72c834a3250b");
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
                .extracting(Consent::getStatus, Consent::getAccounts)
                .containsExactly(Consent.StatusEnum.CONSUMED, null);
        assertThat(consent.getPayments())
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(Payment::getPaymentId, Payment::getType, Payment::getStatus, Payment::getRefunds,
                        Payment::getDetail)
                .containsExactly(UUID.fromString("76e659b6-1994-46cf-a572-38f500aff650"), Payment.TypeEnum.SINGLE,
                        Payment.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED, Collections.emptyList(),
                        new PaymentRequest().consentId(UUID.fromString("b9d04c2f-eea2-44cb-bf9f-72c834a3250b")));
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
                .isInstanceOf(DecoupledFlow.class);
        DecoupledFlow flow = (DecoupledFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(DecoupledFlow::getType, DecoupledFlow::getBank,
                        DecoupledFlow::getIdentifierType, DecoupledFlow::getIdentifierValue)
                .containsExactly(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ,
                        IdentifierType.PHONE_NUMBER, "+6449144425");
        assertThat(detail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(detail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.50");
    }
}
