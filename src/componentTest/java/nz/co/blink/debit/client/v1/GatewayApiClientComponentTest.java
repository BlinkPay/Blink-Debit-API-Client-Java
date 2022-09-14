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
import nz.co.blink.debit.dto.v1.AvailableBank;
import nz.co.blink.debit.dto.v1.AvailableFlow;
import nz.co.blink.debit.dto.v1.AvailableIdentifier;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayConsentRequest;
import nz.co.blink.debit.dto.v1.GatewayConsentResponse;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The component test case for {@link GatewayApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {GatewayApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GatewayApiClientComponentTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    @Autowired
    private GatewayApiClient client;

    @Value("${blinkpay.bff.shared.secret}")
    private String sharedSecret;

    private static AvailableBank bnz;

    private static AvailableBank pnz;

    private static AvailableBank westpac;

    @BeforeAll
    static void setUp() {
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

    @Test
    @DisplayName("Verify that single consent with gateway flow is retrieved")
    @Order(1)
    void getConsent() {
        UUID consentId = UUID.fromString("d97a2df5-8b99-4c77-be3e-f996334937b7");
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
                .containsExactly(Amount.CurrencyEnum.NZD, "1.50");
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
    @DisplayName("Verify that single consent with gateway flow is updated with redirect flow hint")
    @Order(2)
    void updateConsentWithRedirectFlowHint() {
        UUID consentId = UUID.fromString("d97a2df5-8b99-4c77-be3e-f996334937b7");
        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.updateConsent(UUID.randomUUID().toString(),
                sharedSecret, consentId, Bank.PNZ, GatewayConsentRequest.FlowEnum.REDIRECT, null, null, null);

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
                .isEqualTo("https://api-nomatls.apicentre.middleware.co.nz/middleware-nz-sandbox/v2.0/oauth/authorize?scope=openid%20payments&response_type=code%20id_token&request=eyJraWQiOiJCbGlua1BheV9zYW5kYm94X3NpZ25pbmdfa2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUFMyNTYifQ.eyJhdWQiOiJodHRwczpcL1wvYXBpLW5vbWF0bHMuYXBpY2VudHJlLm1pZGRsZXdhcmUuY28ubnpcL21pZGRsZXdhcmUtbnotc2FuZGJveFwvdjIuMFwvb2F1dGhcL2F1dGhvcml6ZT8iLCJzY29wZSI6Im9wZW5pZCBwYXltZW50cyIsImlzcyI6IktNeHBFdW9UTnFEWkN0RzRlNnF2S2g5cUZTR0dXb1oxIiwiY2xhaW1zIjp7ImlkX3Rva2VuIjp7ImFjciI6eyJ2YWx1ZSI6InVybjpvcGVuYmFua2luZzpuejpjYSIsImVzc2VudGlhbCI6dHJ1ZX0sIkNvbnNlbnRJZCI6eyJ2YWx1ZSI6IjlhYTFiNTUwLWJiZjctNDYwMS04NDViLTkxYzU5MDk5MzA0MiIsImVzc2VudGlhbCI6dHJ1ZX19fSwicmVzcG9uc2VfdHlwZSI6ImNvZGUgaWRfdG9rZW4iLCJyZWRpcmVjdF91cmkiOiJodHRwczpcL1wvc2FuZGJveC5kZWJpdC5ibGlua3BheS5jby5uelwvYmFua1wvMS4wXC9yZXR1cm4iLCJzdGF0ZSI6ImQ5N2EyZGY1LThiOTktNGM3Ny1iZTNlLWY5OTYzMzQ5MzdiNyIsImV4cCI6MTY2MzEzMzA4NSwiaWF0IjoxNjYzMTMyMzY1LCJub25jZSI6ImFlOTk2MTEzLTFjNTYtNGUyOC05MGVjLTFmNjJjZjI0ZWFmMyIsImNsaWVudF9pZCI6IktNeHBFdW9UTnFEWkN0RzRlNnF2S2g5cUZTR0dXb1oxIn0.fFOiCjhxaGHLsikXq9_wmONiLDFSJm4alTchvrzWQLq7anUPgDV_VeJFbJI79LFgC59rMtDPx3WLn5yVJK3Bdwuupfv314hBj1AOpWWr9yTN_S_GAK61YgALJmA3hmcXVwSrJ9bUCoIINYcn6u1wNRxN5yKQIrStbZ7FPKubGnRvktGKrXixJrMbq1D8Lqq0kIiyICQl4nw9sdoMkH5hP1ku37WCLe5AsAr6ROEnC8OwCdCe9beO57vry58INV_x0xq17Yo_wuvFYCuC4NTOs4yTaak2dxl411UtfJD7xmRBjuvrAPnCg8IhSVa7LAdm1yo4JZM4l0fKEDirHahDqd9_LwwBCDW70F3sgnRNsi3LyZxqCc4IBFZ1J383QdgxREGUM5SLvNotPYfFN5--Qi0pzlV58BdxdUQRHL6AXiUfNEFoGAW6XMH4oUvb_0CqIhTCXjR-or9pTRRE4rRVD565ByVmhHTwkG9heM5L1CntY9prB3QnOdkMYuIYw1kqXoumh-qE0fe6nL3wVGEtDdeQX9m84kY_DWmkwG1xj35L-fV8l-Qv9D9q3vh4251OfyIxZQRds5JMj2n3ixSxO6jL-i3q0V4dzTYVWaSxdy3EChC0uwgyYHR9EzdGjqmA8MbeRtRnwEyaoEa-VMwcaV1RjjcNBsKYqWsPtUPNqy8&state=d97a2df5-8b99-4c77-be3e-f996334937b7&nonce=ae996113-1c56-4e28-90ec-1f62cf24eaf3&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn&client_id=KMxpEuoTNqDZCtG4e6qvKh9qFSGGWoZ1");
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
                .containsExactly(Amount.CurrencyEnum.NZD, "1.50");
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
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.PNZ);
        assertThat(actual.getAvailableBanks())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder(bnz, pnz, westpac);
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow is retrieved")
    @Order(3)
    void getQuickPayment() {
        UUID quickPaymentId = UUID.fromString("5495f4c2-161c-42bc-83b1-38fc87790380");
        Mono<QuickPaymentResponse> quickPaymentResponseMono = client.getQuickPayment(UUID.randomUUID().toString(),
                sharedSecret, quickPaymentId);

        assertThat(quickPaymentResponseMono).isNotNull();
        QuickPaymentResponse actual = quickPaymentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
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
                .containsExactly(Amount.CurrencyEnum.NZD, "1.50");
        assertThat(consentRequest.getFlow()).isNotNull();
        assertThat(consentRequest.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) consentRequest.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri, GatewayFlow::getFlowHint)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI, null);
    }

    @Test
    @DisplayName("Verify that quick payment with gateway flow is updated with decoupled flow hint")
    @Order(4)
    void updateQuickPaymentWithDecoupledFlow() {
        UUID quickPaymentId = UUID.fromString("5495f4c2-161c-42bc-83b1-38fc87790380");
        Mono<GatewayConsentResponse> gatewayConsentResponseMono = client.updateConsent(UUID.randomUUID().toString(),
                sharedSecret, quickPaymentId, Bank.PNZ, GatewayConsentRequest.FlowEnum.DECOUPLED, IdentifierType.CONSENT_ID,
                "f71900df-2528-4847-8692-f1db6864e4ae", null);

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
                .containsExactly(Amount.CurrencyEnum.NZD, "1.50");
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
}
