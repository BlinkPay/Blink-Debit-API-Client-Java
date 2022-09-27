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

import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.AuthFlow;
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.OneOfauthFlowDetail;
import nz.co.blink.debit.dto.v1.OneOfconsentDetail;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static nz.co.blink.debit.enums.BlinkDebitConstant.QUICK_PAYMENTS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.SINGLE_CONSENTS_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link QuickPaymentsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class QuickPaymentsApiClientTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AccessTokenHandler accessTokenHandler;

    @InjectMocks
    private QuickPaymentsApiClient client;

    @Test
    @DisplayName("Verify that null authorisation flow hint is handled")
    void createQuickPaymentWithGatewayFlowAndNullFlowHint() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, "http://localhost:8080",
                        "particulars", "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Gateway flow type requires redirect or decoupled flow hint type");
    }

    @Test
    @DisplayName("Verify that null bank is handled")
    void createQuickPaymentWithNullBank() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.REDIRECT, null, "http://localhost:8080",
                        "particulars", "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Bank must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow is handled")
    void createQuickPaymentWithNullAuthorisationFlow() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(null, Bank.PNZ,
                        "http://localhost:8080", "particulars", "code", "reference", "1.25", null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that redirect flow with blank redirect URI is handled")
    void createQuickPaymentWithRedirectFlowAndBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, redirectUri, "particulars",
                        "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that decoupled flow with null identifier type is handled")
    void createQuickPaymentWithDecoupledFlowAndNullIdentifierType() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.createQuickPayment(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, "particulars",
                                "code", "reference", "1.25", null, null, "+6449144425", "callbackUrl").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank redirect URI is handled")
    void createQuickPaymentWithDecoupledFlowAndBlankIdentifierValue(String identifierValue) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, "particulars",
                        "code", "reference", "1.25", null, IdentifierType.PHONE_NUMBER, identifierValue,
                        "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank callback URL is handled")
    void createQuickPaymentWithDecoupledFlowAndBlankCallbackUrl(String callbackUrl) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, "particulars",
                        "code", "reference", "1.25", null, IdentifierType.PHONE_NUMBER, "+6449144425",
                        callbackUrl).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Callback/webhook URL must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank redirect URI is handled")
    void createQuickPaymentWithGatewayFlowAndBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.createQuickPayment(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, redirectUri, "particulars", "code",
                                "reference", "1.25", FlowHint.TypeEnum.REDIRECT, null, null, null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that gateway flow with null identifier type is handled")
    void createQuickPaymentWithGatewayFlowAndNullIdentifierType() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, null, "particulars",
                        "code", "reference", "1.25", FlowHint.TypeEnum.DECOUPLED, null, "+6449144425",
                        "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank redirect URI is handled")
    void createQuickPaymentWithGatewayFlowAndBlankIdentifierValue(String identifierValue) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, null, "particulars", "code",
                        "reference", "1.25", FlowHint.TypeEnum.DECOUPLED, IdentifierType.PHONE_NUMBER, identifierValue,
                        null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that invalid particulars is handled")
    void createQuickPaymentWithInvalidParticulars(String particulars) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080",
                        particulars, "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createQuickPaymentWithInvalidTotal(String total) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createQuickPayment(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080",
                        "particulars", "code", "reference", total, null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Total is not a valid amount");
    }

    @Test
    @DisplayName("Verify that quick payment is created")
    void createQuickPayment() {
        UUID quickPaymentId = UUID.randomUUID();
        CreateQuickPaymentResponse response = new CreateQuickPaymentResponse()
                .quickPaymentId(quickPaymentId)
                .redirectUri(REDIRECT_URI);

        when(webClientBuilder.filter(any(ExchangeFilterFunction.class))).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(QUICK_PAYMENTS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(SingleConsentRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        Mono<CreateQuickPaymentResponse> createQuickPaymentResponseMono =
                client.createQuickPayment(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, REDIRECT_URI, "particulars",
                        "code", "reference", "1.25", null);

        assertThat(createQuickPaymentResponseMono).isNotNull();
        CreateQuickPaymentResponse actual = createQuickPaymentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateQuickPaymentResponse::getQuickPaymentId, CreateQuickPaymentResponse::getRedirectUri)
                .containsExactly(quickPaymentId, REDIRECT_URI);
    }

    @Test
    @DisplayName("Verify that null quick payment ID is handled")
    void getQuickPaymentWithNullQuickPaymentId() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getQuickPayment(null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Quick payment ID must not be null");
    }

    @Test
    @DisplayName("Verify that quick payment is retrieved")
    void getQuickPayment() {
        UUID quickPaymentId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Pacific/Auckland"));
        QuickPaymentResponse response = new QuickPaymentResponse()
                .quickPaymentId(quickPaymentId)
                .consent(new Consent()
                        .consentId(quickPaymentId)
                        .status(Consent.StatusEnum.AWAITINGAUTHORISATION)
                        .creationTimestamp(now.minusMinutes(5))
                        .statusUpdatedTimestamp(now)
                        .detail((OneOfconsentDetail) new SingleConsentRequest()
                                .flow(new AuthFlow()
                                        .detail((OneOfauthFlowDetail) new RedirectFlow()
                                                .bank(Bank.PNZ)
                                                .redirectUri(REDIRECT_URI)
                                                .type(AuthFlowDetail.TypeEnum.REDIRECT)))
                                .pcr(new Pcr()
                                        .particulars("particulars")
                                        .code("code")
                                        .reference("reference"))
                                .amount(new Amount()
                                        .currency(Amount.CurrencyEnum.NZD)
                                        .total("1.25"))
                                .type(ConsentDetail.TypeEnum.SINGLE))
                        .payments(Collections.emptySet()));

        when(webClientBuilder.filter(any(ExchangeFilterFunction.class))).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        Mono<QuickPaymentResponse> quickPaymentResponseMono = client.getQuickPayment(quickPaymentId);

        assertThat(quickPaymentResponseMono).isNotNull();
        QuickPaymentResponse actual = quickPaymentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(QuickPaymentResponse::getQuickPaymentId)
                .isEqualTo(quickPaymentId);
        Consent consent = actual.getConsent();
        assertThat(consent)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
        assertThat(consent.getCreationTimestamp()).isEqualTo(now.minusMinutes(5));
        assertThat(consent.getStatusUpdatedTimestamp()).isEqualTo(now);
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
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, REDIRECT_URI);
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
    @DisplayName("Verify that null quick payment ID is handled")
    void revokeQuickPaymentWithNullQuickPaymentId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.revokeQuickPayment(null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Quick payment ID must not be null");
    }

    @Test
    @DisplayName("Verify that quick payment is revoked")
    void revokeQuickPayment() {
        UUID quickPaymentId = UUID.randomUUID();
        when(webClientBuilder.filter(accessTokenHandler.setAccessToken(any(String.class))))
                .thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.empty());

        assertThatNoException().isThrownBy(() -> client.revokeQuickPayment(quickPaymentId).block());
    }
}
