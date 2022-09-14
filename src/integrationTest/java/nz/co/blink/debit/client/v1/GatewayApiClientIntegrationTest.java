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
import nz.co.blink.debit.dto.v1.AvailableBank;
import nz.co.blink.debit.dto.v1.AvailableFlow;
import nz.co.blink.debit.dto.v1.AvailableIdentifier;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayConsentRequest;
import nz.co.blink.debit.dto.v1.GatewayConsentResponse;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import nz.co.blink.debit.exception.ApiError;
import nz.co.blink.debit.exception.ApiException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * The integration test for {@link GatewayApiClient}.
 */
@SpringBootTest(classes = {OAuthApiClient.class, GatewayApiClient.class, SingleConsentsApiClient.class,
        QuickPaymentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@ActiveProfiles("test")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GatewayApiClientIntegrationTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    @Autowired
    private OAuthApiClient oAuthApiClient;

    @Autowired
    private SingleConsentsApiClient singleConsentsApiClient;

    @Autowired
    private QuickPaymentsApiClient quickPaymentsApiClient;

    @Autowired
    private GatewayApiClient client;

    @Value("${blinkpay.bff.shared.secret}")
    private String sharedSecret;

    private static String accessToken;

    private static AvailableBank bnz;

    private static AvailableBank pnz;

    private static AvailableBank westpac;

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

            AvailableFlow redirectFlow = new AvailableFlow()
                    .flow(AvailableFlow.FlowEnum.REDIRECT);
            AvailableFlow bnzDecoupledFlow = new AvailableFlow()
                    .flow(AvailableFlow.FlowEnum.DECOUPLED)
                    .availableIdentifiers(Stream.of(new AvailableIdentifier()
                                    .name("Consent ID")
                                    .type(IdentifierType.CONSENT_ID))
                            .collect(Collectors.toList()));
            AvailableFlow pnzDecoupledFlow = new AvailableFlow()
                    .flow(AvailableFlow.FlowEnum.DECOUPLED)
                    .availableIdentifiers(Stream.of(
                                    new AvailableIdentifier()
                                            .name("Phone Number")
                                            .type(IdentifierType.PHONE_NUMBER),
                                    new AvailableIdentifier()
                                            .name("Mobile Number")
                                            .type(IdentifierType.MOBILE_NUMBER))
                            .collect(Collectors.toList()));
            bnz = new AvailableBank()
                    .bank(Bank.BNZ)
                    .availableFlows(Stream.of(redirectFlow, bnzDecoupledFlow).collect(Collectors.toList()));
            pnz = new AvailableBank()
                    .bank(Bank.PNZ)
                    .availableFlows(Stream.of(redirectFlow, pnzDecoupledFlow).collect(Collectors.toList()));
            westpac = new AvailableBank()
                    .bank(Bank.WESTPAC)
                    .availableFlows(Stream.of(redirectFlow).collect(Collectors.toList()));
        }
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow and redirect flow hint is retrieved")
    @Order(1)
    void getConsentWithRedirectFlowHint() throws ExpiredAccessTokenException {
        UUID consentId = createConsent();

        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.getConsent(UUID.randomUUID().toString(),
                sharedSecret, consentId);

        assertThat(gatewayConsentResponseMono).isNotNull();
        GatewayConsentResponse actual = gatewayConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(GatewayConsentResponse::getMerchantName, GatewayConsentResponse::getMerchantAccountNumber,
                        GatewayConsentResponse::isQuickPayment, GatewayConsentResponse::getMerchantRedirectUri,
                        GatewayConsentResponse::getGatewayRedirectUri, GatewayConsentResponse::getBankRedirectUri)
                .containsExactly("ACME Incorporated", "02-1285-0026988-00", false,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page?cid=" + consentId,
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + consentId, null);
        assertThat(actual.getConsent())
                .isNotNull()
                .extracting(Consent::getConsentId, Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(consentId, Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, null, Collections.emptySet());
        assertThat(actual.getConsent().getCreationTimestamp()).isNotNull();
        assertThat(actual.getConsent().getStatusUpdatedTimestamp()).isNull();
        assertThat(actual.getConsent().getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest consentRequest = (SingleConsentRequest) actual.getConsent().getDetail();
        assertThat(consentRequest.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(consentRequest.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(consentRequest.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
        assertThat(consentRequest.getFlow()).isNotNull();
        assertThat(consentRequest.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) consentRequest.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri, GatewayFlow::getFlowHint)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI, null);
        assertThat(actual.getAvailableBanks())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder(bnz, pnz, westpac);
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow and redirect flow hint is updated for BNZ")
    @Order(2)
    void updateConsentWithRedirectFlowHintForBnz() throws ExpiredAccessTokenException {
        UUID consentId = createConsent();

        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.updateConsent(UUID.randomUUID().toString(),
                sharedSecret, consentId, Bank.BNZ, GatewayConsentRequest.FlowEnum.REDIRECT, null, null, null);

        assertThat(gatewayConsentResponseMono).isNotNull();
        GatewayConsentResponse actual = gatewayConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(GatewayConsentResponse::getMerchantName, GatewayConsentResponse::getMerchantAccountNumber,
                        GatewayConsentResponse::isQuickPayment, GatewayConsentResponse::getMerchantRedirectUri,
                        GatewayConsentResponse::getGatewayRedirectUri)
                .containsExactly("ACME Incorporated", "02-1285-0026988-00", false,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page?cid=" + consentId,
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + consentId);
        assertThat(actual.getBankRedirectUri())
                .isNotBlank()
                .startsWith("https://secure.sandbox.bnz.co.nz/pingfederate/as/authorization.oauth2"
                        + "?scope=payments%20openid&response_type=code%20id_token")
                .contains("&request=", "&state=", "&nonce=")
                .endsWith("&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn"
                        + "&client_id=qVAnzBU96TemgkatLe8R16yn3Wm8KFoU");
        assertThat(actual.getConsent())
                .isNotNull()
                .extracting(Consent::getConsentId, Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(consentId, Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
        assertThat(actual.getConsent().getCreationTimestamp()).isNotNull();
        assertThat(actual.getConsent().getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getConsent().getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest consentRequest = (SingleConsentRequest) actual.getConsent().getDetail();
        assertThat(consentRequest.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(consentRequest.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(consentRequest.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
        assertThat(consentRequest.getFlow()).isNotNull();
        assertThat(consentRequest.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) consentRequest.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .extracting(FlowHint::getType, FlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.BNZ);
        assertThat(actual.getAvailableBanks())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder(bnz, pnz, westpac);
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow and redirect flow hint is updated for Westpac")
    @Order(3)
    void updateConsentWithRedirectFlowHintForWestpac() throws ExpiredAccessTokenException {
        UUID consentId = createConsent();

        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.updateConsent(UUID.randomUUID().toString(),
                sharedSecret, consentId, Bank.WESTPAC, GatewayConsentRequest.FlowEnum.REDIRECT, null, null,
                UUID.randomUUID());

        assertThat(gatewayConsentResponseMono).isNotNull();
        GatewayConsentResponse actual = gatewayConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(GatewayConsentResponse::getMerchantName, GatewayConsentResponse::getMerchantAccountNumber,
                        GatewayConsentResponse::isQuickPayment, GatewayConsentResponse::getMerchantRedirectUri,
                        GatewayConsentResponse::getGatewayRedirectUri)
                .containsExactly("ACME Incorporated", "02-1285-0026988-00", false,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page?cid=" + consentId,
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + consentId);
        assertThat(actual.getBankRedirectUri())
                .isNotBlank()
                .startsWith("https://bank.uat.westpac.co.nz/tokenauth/public/secure-me/index.html"
                        + "?context_type=one_time_payment"
                        + "&original-url=https%3A%2F%2Fconnect.uat.westpac.co.nz%2Fconsumer%2Fdfa%2Fv1%2Fauthorize%3Fclient_id%3Db3aac77b-9053-4072-aea5-4e89bd20488d")
                .contains("&app-name=BlinkPay+DFA+-+Staging", "&appid=616e38e4e4b0aab14dce3cb5", "&org=blinkpay",
                        "&orgtitle=BlinkPay", "&orgid=6168c468e4b0aab14dcdf22a", "&provider=westpac",
                        "&providertitle=Westpac", "&providerid=588a56a5e4b01f17f71aa328", "&catalog=uat",
                        "&catalogtitle=UAT", "&catalogid=588a60f1e4b01f17f71aa394", "&g-transid=", "&transid=");
        assertThat(actual.getConsent())
                .isNotNull()
                .extracting(Consent::getConsentId, Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(consentId, Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, null, Collections.emptySet());
        assertThat(actual.getConsent().getCreationTimestamp()).isNotNull();
        assertThat(actual.getConsent().getStatusUpdatedTimestamp()).isNull();
        assertThat(actual.getConsent().getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest consentRequest = (SingleConsentRequest) actual.getConsent().getDetail();
        assertThat(consentRequest.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(consentRequest.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(consentRequest.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
        assertThat(consentRequest.getFlow()).isNotNull();
        assertThat(consentRequest.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) consentRequest.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .extracting(FlowHint::getType, FlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.WESTPAC);
        assertThat(actual.getAvailableBanks())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder(bnz, pnz, westpac);
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow and decoupled flow hint is updated for PNZ")
    @Order(4)
    void updateConsentWithDecoupledFlowHintForPnz() throws ExpiredAccessTokenException {
        UUID consentId = createConsent();

        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.updateConsent(UUID.randomUUID().toString(),
                sharedSecret, consentId, Bank.PNZ, GatewayConsentRequest.FlowEnum.DECOUPLED, IdentifierType.PHONE_NUMBER,
                "+6449144425", null);

        assertThat(gatewayConsentResponseMono).isNotNull();
        GatewayConsentResponse actual = gatewayConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(GatewayConsentResponse::getMerchantName, GatewayConsentResponse::getMerchantAccountNumber,
                        GatewayConsentResponse::isQuickPayment, GatewayConsentResponse::getMerchantRedirectUri,
                        GatewayConsentResponse::getGatewayRedirectUri)
                .containsExactly("ACME Incorporated", "02-1285-0026988-00", false,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page?cid=" + consentId,
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + consentId);
        assertThat(actual.getBankRedirectUri()).isBlank();
        assertThat(actual.getConsent())
                .isNotNull()
                .extracting(Consent::getConsentId, Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(consentId, Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
        assertThat(actual.getConsent().getCreationTimestamp()).isNotNull();
        assertThat(actual.getConsent().getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getConsent().getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest consentRequest = (SingleConsentRequest) actual.getConsent().getDetail();
        assertThat(consentRequest.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(consentRequest.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(consentRequest.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
        assertThat(consentRequest.getFlow()).isNotNull();
        assertThat(consentRequest.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) consentRequest.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(DecoupledFlowHint.class);
        DecoupledFlowHint flowHint = (DecoupledFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .extracting(DecoupledFlowHint::getType, DecoupledFlowHint::getBank,
                        DecoupledFlowHint::getIdentifierType, DecoupledFlowHint::getIdentifierValue)
                .containsExactly(FlowHint.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER, "+6449144425");
        assertThat(actual.getAvailableBanks())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder(bnz, pnz, westpac);
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is retrieved")
    @Order(5)
    void getQuickPayment() throws ExpiredAccessTokenException {
        UUID quickPaymentId = createQuickPayment();

        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.getConsent(UUID.randomUUID().toString(),
                sharedSecret, quickPaymentId);

        assertThat(gatewayConsentResponseMono).isNotNull();
        GatewayConsentResponse actual = gatewayConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(GatewayConsentResponse::getMerchantName, GatewayConsentResponse::getMerchantAccountNumber,
                        GatewayConsentResponse::isQuickPayment, GatewayConsentResponse::getMerchantRedirectUri,
                        GatewayConsentResponse::getGatewayRedirectUri, GatewayConsentResponse::getBankRedirectUri)
                .containsExactly("ACME Incorporated", "02-1285-0026988-00", true,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page?cid=" + quickPaymentId,
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + quickPaymentId, null);
        assertThat(actual.getConsent())
                .isNotNull()
                .extracting(Consent::getConsentId, Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(quickPaymentId, Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, null, Collections.emptySet());
        assertThat(actual.getConsent().getCreationTimestamp()).isNotNull();
        assertThat(actual.getConsent().getStatusUpdatedTimestamp()).isNull();
        assertThat(actual.getConsent().getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest consentRequest = (SingleConsentRequest) actual.getConsent().getDetail();
        assertThat(consentRequest.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(consentRequest.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(consentRequest.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
        assertThat(consentRequest.getFlow()).isNotNull();
        assertThat(consentRequest.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) consentRequest.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri, GatewayFlow::getFlowHint)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI, null);
        assertThat(actual.getAvailableBanks())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder(bnz, pnz, westpac);
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint is updated for BNZ")
    @Order(6)
    void updateQuickPaymentWithRedirectFlowHintForBnz() throws ExpiredAccessTokenException {
        UUID quickPaymentId = createQuickPayment();

        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.updateConsent(UUID.randomUUID().toString(),
                sharedSecret, quickPaymentId, Bank.BNZ, GatewayConsentRequest.FlowEnum.REDIRECT, null, null,
                null);

        assertThat(gatewayConsentResponseMono).isNotNull();
        GatewayConsentResponse actual = gatewayConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(GatewayConsentResponse::getMerchantName, GatewayConsentResponse::getMerchantAccountNumber,
                        GatewayConsentResponse::isQuickPayment, GatewayConsentResponse::getMerchantRedirectUri,
                        GatewayConsentResponse::getGatewayRedirectUri)
                .containsExactly("ACME Incorporated", "02-1285-0026988-00", true,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page?cid=" + quickPaymentId,
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + quickPaymentId);
        assertThat(actual.getBankRedirectUri())
                .isNotBlank()
                .startsWith("https://secure.sandbox.bnz.co.nz/pingfederate/as/authorization.oauth2"
                        + "?scope=payments%20openid&response_type=code%20id_token")
                .contains("&request=", "&state=", "&nonce=")
                .endsWith("&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn"
                        + "&client_id=qVAnzBU96TemgkatLe8R16yn3Wm8KFoU");
        assertThat(actual.getConsent())
                .isNotNull()
                .extracting(Consent::getConsentId, Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(quickPaymentId, Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
        assertThat(actual.getConsent().getCreationTimestamp()).isNotNull();
        assertThat(actual.getConsent().getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getConsent().getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest consentRequest = (SingleConsentRequest) actual.getConsent().getDetail();
        assertThat(consentRequest.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(consentRequest.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(consentRequest.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
        assertThat(consentRequest.getFlow()).isNotNull();
        assertThat(consentRequest.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) consentRequest.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .extracting(FlowHint::getType, FlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.BNZ);
        assertThat(actual.getAvailableBanks())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder(bnz, pnz, westpac);
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and redirect flow hint for Westpac is handled")
    @Order(7)
    void updateQuickPaymentWithRedirectFlowHintForWestpacThenThrowException() throws ExpiredAccessTokenException {
        UUID quickPaymentId = createQuickPayment();

        RuntimeException exception = catchThrowableOfType(() -> client.updateConsent(UUID.randomUUID().toString(),
                sharedSecret, quickPaymentId, Bank.WESTPAC, GatewayConsentRequest.FlowEnum.REDIRECT, null, null,
                UUID.randomUUID()).block(), RuntimeException.class);

        assertThat(exception).isNotNull();
        assertThat(((ApiException) exception.getCause()).getApiError())
                .isNotNull()
                .extracting(ApiError::getStatus, ApiError::getCode, ApiError::getReason, ApiError::getCause,
                        ApiError::getDescription)
                .containsExactly(HttpStatus.UNPROCESSABLE_ENTITY, "BP005", "UNPROCESSABLE_ENTITY", null,
                        "Consent is not yet authorised");
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow and decoupled flow hint is updated for PNZ")
    @Order(8)
    void updateQuickPaymentWithDecoupledFlowHintForPnz() throws ExpiredAccessTokenException {
        UUID quickPaymentId = createQuickPayment();

        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.updateConsent(UUID.randomUUID().toString(),
                sharedSecret, quickPaymentId, Bank.PNZ, GatewayConsentRequest.FlowEnum.DECOUPLED,
                IdentifierType.PHONE_NUMBER, "+6449144425", null);

        assertThat(gatewayConsentResponseMono).isNotNull();
        GatewayConsentResponse actual = gatewayConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(GatewayConsentResponse::getMerchantName, GatewayConsentResponse::getMerchantAccountNumber,
                        GatewayConsentResponse::isQuickPayment, GatewayConsentResponse::getMerchantRedirectUri,
                        GatewayConsentResponse::getGatewayRedirectUri)
                .containsExactly("ACME Incorporated", "02-1285-0026988-00", true,
                        "https://www.blinkpay.co.nz/sample-merchant-return-page?cid=" + quickPaymentId,
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + quickPaymentId);
        assertThat(actual.getBankRedirectUri()).isBlank();
        assertThat(actual.getConsent())
                .isNotNull()
                .extracting(Consent::getConsentId, Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(quickPaymentId, Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
        assertThat(actual.getConsent().getCreationTimestamp()).isNotNull();
        assertThat(actual.getConsent().getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getConsent().getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest consentRequest = (SingleConsentRequest) actual.getConsent().getDetail();
        assertThat(consentRequest.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(consentRequest.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(consentRequest.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "1.25");
        assertThat(consentRequest.getFlow()).isNotNull();
        assertThat(consentRequest.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) consentRequest.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI);
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(DecoupledFlowHint.class);
        DecoupledFlowHint flowHint = (DecoupledFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .extracting(DecoupledFlowHint::getType, DecoupledFlowHint::getBank,
                        DecoupledFlowHint::getIdentifierType, DecoupledFlowHint::getIdentifierValue)
                .containsExactly(FlowHint.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER, "+6449144425");
        assertThat(actual.getAvailableBanks())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder(bnz, pnz, westpac);
    }

    private UUID createConsent() throws ExpiredAccessTokenException {
        Mono<CreateConsentResponse> createConsentResponseMono = singleConsentsApiClient.createSingleConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, null, REDIRECT_URI, "particulars", "code",
                "reference", "1.25");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse createConsentResponse = createConsentResponseMono.block();
        assertThat(createConsentResponse).isNotNull();

        UUID consentId = createConsentResponse.getConsentId();
        assertThat(consentId).isNotNull();

        assertThat(createConsentResponse.getRedirectUri())
                .isNotBlank()
                .isEqualTo("https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + consentId);

        return consentId;
    }

    private UUID createQuickPayment() throws ExpiredAccessTokenException {
        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseMono = quickPaymentsApiClient.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, null, REDIRECT_URI, "particulars", "code",
                "reference", "1.25", null);

        assertThat(createQuickPaymentResponseMono).isNotNull();
        CreateQuickPaymentResponse createConsentResponse = createQuickPaymentResponseMono.block();
        assertThat(createConsentResponse).isNotNull();

        UUID quickPaymentId = createConsentResponse.getQuickPaymentId();
        assertThat(quickPaymentId).isNotNull();

        assertThat(createConsentResponse.getRedirectUri())
                .isNotBlank()
                .isEqualTo("https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=" + quickPaymentId);

        return quickPaymentId;
    }
}
