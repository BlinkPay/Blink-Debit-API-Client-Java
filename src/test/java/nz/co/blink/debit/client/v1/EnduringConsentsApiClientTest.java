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
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.OneOfauthFlowDetail;
import nz.co.blink.debit.dto.v1.OneOfconsentDetail;
import nz.co.blink.debit.dto.v1.Period;
import nz.co.blink.debit.dto.v1.RedirectFlow;
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
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static nz.co.blink.debit.enums.BlinkDebitConstant.ENDURING_CONSENTS_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link EnduringConsentsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class EnduringConsentsApiClientTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

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

    @Mock
    private ReactorClientHttpConnector connector;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AccessTokenHandler accessTokenHandler;

    @InjectMocks
    private EnduringConsentsApiClient client;

    @Test
    @DisplayName("Verify that null authorisation flow hint is handled")
    void createEnduringConsentWithGatewayFlowAndNullFlowHint() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, "http://localhost:8080",
                        Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER,
                        "+6449144425", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Gateway flow type requires redirect or decoupled flow hint type");
    }

    @Test
    @DisplayName("Verify that null bank is handled")
    void createEnduringConsentWithNullBank() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT, null, "http://localhost:8080",
                                Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Bank must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow is handled")
    void createEnduringConsentWithNullAuthorisationFlow() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(null, Bank.PNZ,
                        "http://localhost:8080", Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that redirect flow with blank redirect URI is handled")
    void createEnduringConsentWithRedirectFlowAndBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, redirectUri,
                                Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that decoupled flow with null identifier type is handled")
    void createEnduringConsentWithDecoupledFlowAndNullIdentifierType() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.MONTHLY,
                        OffsetDateTime.now(ZONE_ID), null, "50.00", null, null, "+6449144425",
                        "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank identifier value is handled")
    void createEnduringConsentWithDecoupledFlowAndBlankIdentifierValue(String identifierValue) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.MONTHLY,
                        OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER, identifierValue,
                        "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank callback URL is handled")
    void createEnduringConsentWithDecoupledFlowAndBlankCallbackUrl(String callbackUrl) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.MONTHLY,
                        OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425",
                        callbackUrl).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Callback/webhook URL must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank redirect URI is handled")
    void createEnduringConsentWithGatewayFlowAndBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, redirectUri,
                                Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.REDIRECT,
                                IdentifierType.PHONE_NUMBER, "+6449144425", "callbackUrl").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that gateway flow with null identifier type is handled")
    void createEnduringConsentWithGatewayFlowAndNullIdentifierType() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, null, Period.MONTHLY,
                        OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.DECOUPLED, null, "+6449144425",
                        "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank identifier value is handled")
    void createEnduringConsentWithGatewayFlowAndBlankIdentifierValue(String identifierValue) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, null, Period.MONTHLY,
                                OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.DECOUPLED,
                                IdentifierType.PHONE_NUMBER, identifierValue, "callbackUrl").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @Test
    @DisplayName("Verify that null period is handled")
    void createEnduringConsentWithNullPeriod() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "redirectUri", null,
                        OffsetDateTime.now(ZONE_ID), null, "50.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Period must not be null");
    }

    @Test
    @DisplayName("Verify that null start date is handled")
    void createEnduringConsentWithNullStartDate() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "redirectUri", Period.MONTHLY,
                        null, null, "50.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Start date must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createEnduringConsentWithInvalidTotal(String total) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "redirectUri", Period.MONTHLY,
                        OffsetDateTime.now(ZONE_ID), null, total).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Total is not a valid amount");
    }

    @Test
    @DisplayName("Verify that enduring consent is created")
    void createEnduringConsent() {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        CreateConsentResponse response = new CreateConsentResponse()
                .consentId(consentId)
                .redirectUri("http://localhost:8080");

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(ENDURING_CONSENTS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(EnduringConsentRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        Mono<CreateConsentResponse> createConsentResponseMono =
                client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080",
                        Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(consentId, "http://localhost:8080");
    }

    @Test
    @DisplayName("Verify that null consent ID is handled")
    void getEnduringConsentWithNullConsentId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.getEnduringConsent(null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Consent ID must not be null");
    }

    @Test
    @DisplayName("Verify that enduring consent is retrieved")
    void getEnduringConsent() {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Pacific/Auckland"));
        Consent consent = new Consent()
                .consentId(consentId)
                .status(Consent.StatusEnum.AWAITINGAUTHORISATION)
                .creationTimestamp(now.minusHours(1))
                .detail((OneOfconsentDetail) new EnduringConsentRequest()
                        .flow(new AuthFlow()
                                .detail((OneOfauthFlowDetail) new RedirectFlow()
                                        .bank(Bank.PNZ)
                                        .redirectUri(REDIRECT_URI)
                                        .type(AuthFlowDetail.TypeEnum.REDIRECT)))
                        .period(Period.MONTHLY)
                        .fromTimestamp(now)
                        .maximumAmountPeriod(new Amount()
                                .currency(Amount.CurrencyEnum.NZD)
                                .total("50.00"))
                        .type(ConsentDetail.TypeEnum.ENDURING))
                .payments(Collections.emptySet());

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(consent));

        Mono<Consent> consentMono = client.getEnduringConsent(consentId);

        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
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
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, REDIRECT_URI);
        assertThat(detail.getPeriod()).isEqualTo(Period.MONTHLY);
        assertThat(detail.getFromTimestamp()).isEqualTo(now);
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that null consent ID is handled")
    void revokeEnduringConsentWithNullConsentId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.revokeEnduringConsent(null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Consent ID must not be null");
    }

    @Test
    @DisplayName("Verify that enduring consent is revoked")
    void revokeEnduringConsent() {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.empty());

        assertThatNoException().isThrownBy(() -> client.revokeEnduringConsent(consentId).block());
    }
}
