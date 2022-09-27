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
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Period;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.helpers.AccessTokenHandler;
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

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * The component test case for {@link EnduringConsentsApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {AccessTokenHandler.class, OAuthApiClient.class, EnduringConsentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnduringConsentsApiClientComponentTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

    @Autowired
    private OAuthApiClient oAuthApiClient;

    @Autowired
    private EnduringConsentsApiClient client;

    @BeforeEach
    void setUp() {
        // use real host to generate valid access token
        ReflectionTestUtils.setField(oAuthApiClient, "webClientBuilder",
                WebClient.builder().baseUrl("https://dev.debit.blinkpay.co.nz"));
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is created")
    @Order(1)
    void createEnduringConsentWithRedirectFlow() {
        Mono<CreateConsentResponse> createConsentResponseMono =
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, REDIRECT_URI,
                        Period.FORTNIGHTLY, OffsetDateTime.now(ZONE_ID), null, "50.00");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("8e916a6f-2a5d-4cb1-8b0b-8e8bb9677458"),
                        "https://api-nomatls.apicentre.middleware.co.nz/middleware-nz-sandbox/v2.0/oauth/authorize?scope=openid%20payments&response_type=code%20id_token&request=header.payload.signature&state=8e916a6f-2a5d-4cb1-8b0b-8e8bb9677458&nonce=b53c608b-bee8-4d31-9962-d8c566994f73&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn&client_id=clientId");
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is retrieved")
    @Order(2)
    void getEnduringConsentWithRedirectFlow() {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.fromString("8e916a6f-2a5d-4cb1-8b0b-8e8bb9677458"));

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

    @Test
    @DisplayName("Verify that enduring consent is revoked")
    @Order(3)
    void revokeEnduringConsent() {
        assertThatNoException().isThrownBy(() ->
                client.revokeEnduringConsent(UUID.fromString("0d48f138-2681-4af1-afeb-3351407b9daa")).block());
    }

    @Test
    @DisplayName("Verify that enduring consent with decoupled flow is created")
    @Order(4)
    void createEnduringConsentWithDecoupledFlow() {
        Mono<CreateConsentResponse> createConsentResponseMono =
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, REDIRECT_URI,
                        Period.FORTNIGHTLY, OffsetDateTime.now(ZONE_ID), null, "50.00", null,
                        IdentifierType.PHONE_NUMBER, "+6449144425", "callbackUrl");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId)
                .isEqualTo(UUID.fromString("294dda40-0357-4970-a86f-5b4974b880aa"));
    }

    @Test
    @DisplayName("Verify that enduring consent with decoupled flow is retrieved")
    @Order(5)
    void getEnduringConsentWithDecoupledFlow() {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.fromString("294dda40-0357-4970-a86f-5b4974b880aa"));

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AUTHORISED, null, Collections.emptySet());
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
                .isNotNull()
                .extracting(DecoupledFlow::getType, DecoupledFlow::getBank, DecoupledFlow::getIdentifierType,
                        DecoupledFlow::getIdentifierValue)
                .containsExactly(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER,
                        "+6449144425");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNotNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow is created")
    @Order(6)
    void createEnduringConsentWithGatewayFlow() {
        Mono<CreateConsentResponse> createConsentResponseMono =
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, REDIRECT_URI,
                        Period.FORTNIGHTLY, OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.DECOUPLED,
                        IdentifierType.PHONE_NUMBER, "+6449144425", "callbackUrl");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("44b7169f-90a0-4b8d-b723-056363a3fe53"),
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=44b7169f-90a0-4b8d-b723-056363a3fe53");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow is retrieved")
    @Order(7)
    void getEnduringConsentWithGatewayFlow() {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.fromString("44b7169f-90a0-4b8d-b723-056363a3fe53"));

        assertThat(consentMono).isNotNull();
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
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri, GatewayFlow::getFlowHint)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI, null);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNotNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }
}
