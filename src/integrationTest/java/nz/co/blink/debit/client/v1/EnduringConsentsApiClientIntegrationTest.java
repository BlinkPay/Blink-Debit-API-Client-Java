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
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Period;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.RedirectFlowHint;
import nz.co.blink.debit.helpers.AccessTokenHandler;
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
@SpringBootTest(classes = {AccessTokenHandler.class, OAuthApiClient.class, EnduringConsentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@ActiveProfiles("test")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnduringConsentsApiClientIntegrationTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

    @Autowired
    private EnduringConsentsApiClient client;

    private static UUID consentId;

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is created in PNZ")
    @Order(1)
    void createEnduringConsentWithRedirectFlowInPnz() {
        Mono<CreateConsentResponse> createConsentResponseMono =
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, REDIRECT_URI,
                        Period.FORTNIGHTLY, OffsetDateTime.now(ZONE_ID), null, "50.00");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual).isNotNull();

        consentId = actual.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(actual.getRedirectUri())
                .isNotBlank()
                .startsWith("https://api-nomatls.apicentre.middleware.co.nz/middleware-nz-sandbox/v2.0/oauth/authorize"
                        + "?scope=openid%20payments&response_type=code%20id_token")
                .contains("&request=", "&state=", "&nonce=")
                .contains("&redirect_uri=https%3A%2F%2Fsandbox.debit.blinkpay.co.nz%2Fbank%2F1.0%2Freturn")
                .contains("&client_id=");
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is retrieved from PNZ")
    @Order(2)
    void getEnduringConsentWithRedirectFlowFromPnz() {
        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

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
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is revoked in PNZ")
    @Order(3)
    void revokeEnduringConsentWithRedirectFlowInPnz() {
        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(consentId).block());

        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

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
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, REDIRECT_URI);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that rejected/timed out enduring consent with redirect flow is retrieved from PNZ")
    @Order(4)
    void getRejectedEnduringConsentWithRedirectFlowFromPnz() {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.fromString("f0b5fc9e-afa2-441f-a9e7-e2131952b835"));

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

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and redirect flow hint is created in PNZ")
    @Order(5)
    void createEnduringConsentWithGatewayFlowAndRedirectFlowHintInPnz() {
        Mono<CreateConsentResponse> createConsentResponseMono =
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, REDIRECT_URI,
                        Period.FORTNIGHTLY, OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.REDIRECT,
                        null, null, null);

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
    @DisplayName("Verify that enduring consent with gateway flow and redirect flow hint is retrieved from PNZ")
    @Order(6)
    void getEnduringConsentWithGatewayFlowAndRedirectFlowHintFromPnz() {
        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

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
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, "https://www.blinkpay.co.nz/sample-merchant-return-page");
        assertThat(flow.getFlowHint())
                .isNotNull()
                .isInstanceOf(RedirectFlowHint.class);
        RedirectFlowHint flowHint = (RedirectFlowHint) flow.getFlowHint();
        assertThat(flowHint)
                .isNotNull()
                .extracting(RedirectFlowHint::getType, RedirectFlowHint::getBank)
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.PNZ);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and redirect flow hint is revoked in PNZ")
    @Order(7)
    void revokeEnduringConsentWithGatewayFlowAndRedirectFlowHintInPnz() {
        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(consentId).block());

        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

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
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
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
                .containsExactly(FlowHint.TypeEnum.REDIRECT, Bank.PNZ);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and decoupled flow hint is created in PNZ")
    @Order(8)
    void createEnduringConsentWithGatewayFlowAndDecoupledFlowHintInPnz() {
        Mono<CreateConsentResponse> createConsentResponseMono =
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, REDIRECT_URI,
                        Period.FORTNIGHTLY, OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.DECOUPLED,
                        IdentifierType.PHONE_NUMBER, "+6449144425", null);

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
    @DisplayName("Verify that enduring consent with gateway flow and decoupled flow hint is retrieved from PNZ")
    @Order(9)
    void getEnduringConsentWithGatewayFlowAndDecoupledFlowHintFromPnz() {
        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

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
                .containsExactly(FlowHint.TypeEnum.DECOUPLED, Bank.PNZ,
                        IdentifierType.PHONE_NUMBER, "+6449144425");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and decoupled flow hint is revoked in PNZ")
    @Order(10)
    void revokeEnduringConsentWithGatewayFlowAndDecoupledFlowHintInPnz() {
        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(consentId).block());

        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

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
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
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
                .containsExactly(FlowHint.TypeEnum.DECOUPLED, Bank.PNZ,
                        IdentifierType.PHONE_NUMBER, "+6449144425");
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
    void createEnduringConsentWithDecoupledFlowInPnz() {
        Mono<CreateConsentResponse> createConsentResponseMono =
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.FORTNIGHTLY,
                        OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425",
                        "https://eout2fipbfh7o93.m.pipedream.net");

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
    void getEnduringConsentWithDecoupledFlowFromPnz() {
        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

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
                        DecoupledFlow::getIdentifierValue, DecoupledFlow::getCallbackUrl)
                .containsExactly(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER,
                        "+6449144425", "https://eout2fipbfh7o93.m.pipedream.net");
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
    void revokeEnduringConsentWithDecoupledFlowInPnz() {
        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(consentId).block());

        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

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
                        DecoupledFlow::getIdentifierValue, DecoupledFlow::getCallbackUrl)
                .containsExactly(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER,
                        "+6449144425", "https://eout2fipbfh7o93.m.pipedream.net");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }
}
