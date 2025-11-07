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
import nz.co.blink.debit.config.BlinkPayProperties;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.AuthFlow;
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.RedirectFlowHint;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import nz.co.blink.debit.service.ValidationService;
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
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * The component test case for {@link SingleConsentsApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {AccessTokenHandler.class, OAuthApiClient.class, SingleConsentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SingleConsentsApiClientComponentTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    @Autowired
    private ReactorClientHttpConnector connector;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private BlinkPayProperties properties;

    private SingleConsentsApiClient client;

    @BeforeEach
    void setUp() {
        // Component tests use WireMock, no real credentials needed
        OAuthApiClient oauthApiClient = new OAuthApiClient(connector, properties);

        client = new SingleConsentsApiClient(connector, properties, new AccessTokenHandler(oauthApiClient),
                validationService);
    }

    @Test
    @DisplayName("Verify that single consent with redirect flow is created")
    @Order(1)
    void createSingleConsentWithRedirectFlow() throws BlinkServiceException {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.BNZ)
                                .redirectUri(REDIRECT_URI)
                                .redirectToApp(true)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .hashedCustomerIdentifier("88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e");

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("27a58af6-5afe-4914-b446-7081f1a14055"),
                        "https://secure.sandbox.bnz.co.nz/pingfederate/as/authorization.oauth2?scope=payments%20openid&response_type=code%20id_token&request=header.payload.signature&state=27a58af6-5afe-4914-b446-7081f1a14055&nonce=81e1afbd-3f4b-484c-8783-483f9c07e4d1&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn&client_id=clientId");
    }

    @Test
    @DisplayName("Verify that single consent with redirect flow is retrieved")
    @Order(2)
    void getSingleConsentWithRedirectFlow() throws BlinkServiceException {
        Mono<Consent> consentMono = client.getSingleConsent(
                UUID.fromString("5ccf243a-af8a-4c75-99b7-671c02cf8566"), UUID.randomUUID().toString());

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getPayments, Consent::getCardNetwork)
                .containsExactly(Consent.StatusEnum.AWAITINGAUTHORISATION, Collections.emptyList(), null);
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
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
    @DisplayName("Verify that single consent is revoked")
    @Order(3)
    void revokeSingleConsent() {
        assertThatNoException().isThrownBy(() ->
                client.revokeSingleConsent(UUID.fromString("0d48f138-2681-4af1-afeb-3351407b9daa")).block());
    }

    @Test
    @DisplayName("Verify that single consent with decoupled flow is created")
    @Order(4)
    void createSingleConsentWithDecoupledFlow() throws BlinkServiceException {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.BNZ)
                                .identifierType(IdentifierType.CONSENT_ID)
                                .identifierValue(UUID.randomUUID().toString())
                                .callbackUrl("callbackUrl")))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .hashedCustomerIdentifier("88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e");

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId)
                .isEqualTo(UUID.fromString("9a456295-36f6-4961-aefc-11d279fbd0cb"));
    }

    @Test
    @DisplayName("Verify that single consent with decoupled flow is retrieved")
    @Order(5)
    void getSingleConsentWithDecoupledFlow() throws BlinkServiceException {
        Mono<Consent> consentMono = client.getSingleConsent(
                UUID.fromString("b4a0de42-6c9a-4ec6-898d-dee18280a7b5"), UUID.randomUUID().toString());

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getPayments, Consent::getCardNetwork)
                .containsExactly(Consent.StatusEnum.AUTHORISED, Collections.emptyList(), null);
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(DecoupledFlow.class);
        DecoupledFlow flow = (DecoupledFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(DecoupledFlow::getType, DecoupledFlow::getBank, DecoupledFlow::getIdentifierType,
                        DecoupledFlow::getIdentifierValue)
                .containsExactly(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER,
                        "+64-259531933");
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
    @DisplayName("Verify that single consent with gateway flow and redirect flow hint is created")
    @Order(6)
    void createSingleConsentWithGatewayFlowAndRedirectFlowHint() throws BlinkServiceException {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.WESTPAC))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .hashedCustomerIdentifier("88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e");

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("58ed876d-5419-405c-a416-f1d77177f93f"),
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=58ed876d-5419-405c-a416-f1d77177f93f");
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow and decoupled flow hint is created")
    @Order(7)
    void createSingleConsentWithGatewayFlowAndDecoupledFlowHint() throws BlinkServiceException {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new DecoupledFlowHint()
                                        .identifierType(IdentifierType.PHONE_NUMBER)
                                        .identifierValue("+64-21835993")
                                        .bank(Bank.ANZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .hashedCustomerIdentifier("88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e");

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("58ed876d-5419-405c-a416-f1d77177f93f"),
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=58ed876d-5419-405c-a416-f1d77177f93f");
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow is retrieved")
    @Order(8)
    void getSingleConsentWithGatewayFlow() throws BlinkServiceException {
        Mono<Consent> consentMono = client.getSingleConsent(
                UUID.fromString("58ed876d-5419-405c-a416-f1d77177f93f"), UUID.randomUUID().toString());

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getPayments, Consent::getCardNetwork)
                .containsExactly(Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, Collections.emptyList(), null);
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(SingleConsentRequest.class);
        SingleConsentRequest detail = (SingleConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.SINGLE);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri, GatewayFlow::getFlowHint)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI, null);
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
