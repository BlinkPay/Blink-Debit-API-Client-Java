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
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.RedirectFlow;
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
 * The component test case for {@link SingleConsentsApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {OAuthApiClient.class, SingleConsentsApiClient.class})
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
    private OAuthApiClient oAuthApiClient;

    @Autowired
    private SingleConsentsApiClient client;

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
                    .startsWith("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjdiZDdGYlpSdC1yT1oxVTRBaEdCbCJ9");
        }
    }

    @Test
    @DisplayName("Verify that single consent with redirect flow is created")
    @Order(1)
    void createSingleConsentWithRedirectFlow() throws ExpiredAccessTokenException {
        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.REDIRECT, Bank.BNZ, REDIRECT_URI, "particulars", "code",
                "reference", "1.25");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("27a58af6-5afe-4914-b446-7081f1a14055"),
                        "https://secure.sandbox.bnz.co.nz/pingfederate/as/authorization.oauth2?scope=payments%20openid&response_type=code%20id_token&request=eyJraWQiOiJCbGlua1BheV9zYW5kYm94X3NpZ25pbmdfa2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUFMyNTYifQ.eyJhdWQiOiJodHRwczpcL1wvc2VjdXJlLnNhbmRib3guYm56LmNvLm56XC9waW5nZmVkZXJhdGUiLCJzY29wZSI6InBheW1lbnRzIG9wZW5pZCIsImlzcyI6InFWQW56QlU5NlRlbWdrYXRMZThSMTZ5bjNXbThLRm9VIiwiY2xhaW1zIjp7ImlkX3Rva2VuIjp7ImFjciI6eyJ2YWx1ZSI6InVybjpvcGVuYmFua2luZzpuejpjYSIsImVzc2VudGlhbCI6dHJ1ZX0sIm9wZW5iYW5raW5nX2ludGVudF9pZCI6eyJ2YWx1ZSI6InVybi1vcGVuYmFua2luZy1ibnotcGF5bWVudHMtMTg0Nzk3IiwiZXNzZW50aWFsIjp0cnVlfX19LCJyZXNwb25zZV90eXBlIjoiY29kZSIsInJlZGlyZWN0X3VyaSI6Imh0dHBzOlwvXC9zYW5kYm94LmRlYml0LmJsaW5rcGF5LmNvLm56XC9iYW5rXC8xLjBcL3JldHVybiIsInN0YXRlIjoiMjdhNThhZjYtNWFmZS00OTE0LWI0NDYtNzA4MWYxYTE0MDU1IiwiZXhwIjoxNjYyOTYwNTUyLCJpYXQiOjE2NjI5NjAxMzIsIm5vbmNlIjoiODFlMWFmYmQtM2Y0Yi00ODRjLTg3ODMtNDgzZjljMDdlNGQxIiwiY2xpZW50X2lkIjoicVZBbnpCVTk2VGVtZ2thdExlOFIxNnluM1dtOEtGb1UifQ.xWYJ8qtX7QcZ4yr3KMWLjRYgFS-mMRfe98vpVgFFgCDjttInK0ADrZ7My-xeiSQN0160-v2cwE1U1s7S6PrXDmVbGBZ49NG4724bmj5d8Uxe8evD4fOEwE_qB7dV196R2ZoNsZL3WYzZqlAJpcBO3SmWC5QHNc-39fT3ejtG26_4r31yDp1XlcZxc4KbejG_AwgEm3a-osV-XKwMwxtWrs4bJPxOaWmsz0a5HFil-FWCZ3b-S5JliPHLvBQuBQrKYUU4hFh5MReTyy9cBIgxk3y8yWRuc25H996irOGR6EaxIHwhgzzSQNfYep2Mo5GUy7hSHH8--OJjOKLu8tqxJh0hs4AOuGbyOqWlt1HcVqxxr3fL7aMQZ7G8G1LkQuFBR-edE1heppndqPsXQU75idWPo1egqIUp6Mw780Lou43w3ax6WY2uLDq9VsQkIxOI3EeUG59y_5q9cUH6si817KNrUNGPTDVC7NUMbEBQTaBqQ6DSGUAogQgSUzt0MKebZMkL6BylpQMzFFjZ9sH4Cpyhi979B6M2Fc4A9QEssfq72Le-Tkpf7059o1LkCzxi_Fnp9dCh9Z2YtkBXozLYxoFnjcfzpAQ-QmUMgQ486yB0fyGTK_sBIjM_RcltRSwNnFivVEL7zEbNJ7bPEuUfkC72EW5Zm3Vp6xIDQP_OFWo&state=27a58af6-5afe-4914-b446-7081f1a14055&nonce=81e1afbd-3f4b-484c-8783-483f9c07e4d1&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn&client_id=qVAnzBU96TemgkatLe8R16yn3Wm8KFoU");
    }

    @Test
    @DisplayName("Verify that single consent with redirect flow is retrieved")
    @Order(2)
    void getSingleConsentWithRedirectFlow() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getSingleConsent(UUID.randomUUID().toString(), accessToken,
                UUID.fromString("5ccf243a-af8a-4c75-99b7-671c02cf8566"));

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
        assertThatNoException().isThrownBy(() -> client.revokeSingleConsent(UUID.randomUUID().toString(), accessToken,
                UUID.fromString("0d48f138-2681-4af1-afeb-3351407b9daa")).block());
    }

    @Test
    @DisplayName("Verify that single consent with decoupled flow is created")
    @Order(4)
    void createSingleConsentWithDecoupledFlow() throws ExpiredAccessTokenException {
        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.DECOUPLED, Bank.BNZ, REDIRECT_URI, "particulars", "code",
                "reference", "1.25");

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
    void getSingleConsentWithDecoupledFlow() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getSingleConsent(UUID.randomUUID().toString(), accessToken,
                UUID.fromString("b4a0de42-6c9a-4ec6-898d-dee18280a7b5"));

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
                        "+6449144425");
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
    @DisplayName("Verify that single consent with gateway flow is created")
    @Order(6)
    void createSingleConsentWithGatewayFlow() throws ExpiredAccessTokenException {
        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.GATEWAY, Bank.WESTPAC, REDIRECT_URI, "particulars", "code",
                "reference", "1.25");

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
    @Order(7)
    void getSingleConsentWithGatewayFlow() throws ExpiredAccessTokenException {
        Mono<Consent> consentMono = client.getSingleConsent(UUID.randomUUID().toString(), accessToken,
                UUID.fromString("58ed876d-5419-405c-a416-f1d77177f93f"));

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
