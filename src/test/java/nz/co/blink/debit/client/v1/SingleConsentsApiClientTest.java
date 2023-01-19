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

import io.github.resilience4j.retry.Retry;
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
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.OneOfauthFlowDetail;
import nz.co.blink.debit.dto.v1.OneOfconsentDetail;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.RedirectFlowHint;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.Validation;
import javax.validation.Validator;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static nz.co.blink.debit.enums.BlinkDebitConstant.SINGLE_CONSENTS_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link SingleConsentsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class SingleConsentsApiClientTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    private static final String CALLBACK_URL = "https://www.mymerchant.co.nz/callback";

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

    @Spy
    private BlinkPayProperties properties = new BlinkPayProperties();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AccessTokenHandler accessTokenHandler;

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Spy
    private Retry retry = Retry.ofDefaults("retry");

    @InjectMocks
    private SingleConsentsApiClient client;

    @Test
    @DisplayName("Verify that null request is handled")
    void createSingleConsentWithRedirectFlowAndNullRequest() {
        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(null).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Single consent request must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow is handled")
    void createSingleConsentWithRedirectFlowAndNullAuthorisationFlow() {
        SingleConsentRequest request = new SingleConsentRequest()
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow detail is handled")
    void createSingleConsentWithRedirectFlowAndNullAuthorisationFlowDetail() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow())
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow detail must not be null");
    }

    @Test
    @DisplayName("Verify that null bank is handled")
    void createSingleConsentWithRedirectFlowAndNullBank() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .redirectUri(REDIRECT_URI)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Bank must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that redirect flow with blank redirect URI is handled")
    void createSingleConsentWithRedirectFlowAndBlankRedirectUri(String redirectUri) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(redirectUri)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that null amount is handled")
    void createSingleConsentWithRedirectFlowAndNullAmount() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(REDIRECT_URI)))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Amount must not be null");
    }

    @Test
    @DisplayName("Verify that null currency is handled")
    void createSingleConsentWithRedirectFlowAndNullCurrency() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(REDIRECT_URI)))
                .amount(new Amount()
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Currency must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createSingleConsentWithRedirectFlowAndInvalidTotal(String total) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(REDIRECT_URI)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total(total))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessageStartingWith("Validation failed for single consent request");
    }

    @Test
    @DisplayName("Verify that null PCR is handled")
    void createSingleConsentWithRedirectFlowAndNullPcr() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(REDIRECT_URI)))
                .amount(new Amount()
                        .total("1.25"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Verify that blank particulars is handled")
    void createSingleConsentWithRedirectFlowAndBlankParticulars(String particulars) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(REDIRECT_URI)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars(particulars)
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @Test
    @DisplayName("Verify that long PCR values are handled")
    void createSingleConsentWithRedirectFlowAndLongPcrValues() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(REDIRECT_URI)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("merchant particulars")
                        .code("merchant code")
                        .reference("merchant reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not exceed 12 characters");
    }

    @Test
    @DisplayName("Verify that null request is handled")
    void createSingleConsentWithDecoupledFlowAndNullRequest() {
        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(null).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Single consent request must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow is handled")
    void createSingleConsentWithDecoupledFlowAndNullAuthorisationFlow() {
        SingleConsentRequest request = new SingleConsentRequest()
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow detail is handled")
    void createSingleConsentWithDecoupledFlowAndNullAuthorisationFlowDetail() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow())
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow detail must not be null");
    }

    @Test
    @DisplayName("Verify that null bank is handled")
    void createSingleConsentWithDecoupledFlowAndNullBank() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Bank must not be null");
    }

    @Test
    @DisplayName("Verify that null amount is handled")
    void createSingleConsentWithDecoupledFlowAndNullAmount() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Amount must not be null");
    }

    @Test
    @DisplayName("Verify that null currency is handled")
    void createSingleConsentWithDecoupledFlowAndNullCurrency() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Currency must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\", "10000000000000.00", "1.123"})
    @DisplayName("Verify that invalid total is handled")
    void createSingleConsentWithDecoupledFlowAndInvalidTotal(String total) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total(total))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessageStartingWith("Validation failed for single consent request");
    }

    @Test
    @DisplayName("Verify that null PCR is handled")
    void createSingleConsentWithDecoupledFlowAndNullPcr() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Verify that blank particulars is handled")
    void createSingleConsentWithDecoupledFlowAndBlankParticulars(String particulars) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars(particulars)
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @Test
    @DisplayName("Verify that long PCR values are handled")
    void createSingleConsentWithDecoupledFlowAndLongPcrValues() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("merchant particulars")
                        .code("merchant code")
                        .reference("merchant reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not exceed 12 characters");
    }

    @Test
    @DisplayName("Verify that decoupled flow with null identifier type is handled")
    void createSingleConsentWithDecoupledFlowAndNullIdentifierType() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank identifier value is handled")
    void createSingleConsentWithDecoupledFlowAndBlankIdentifierValue(String identifierValue) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue(identifierValue)
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank callback URL is handled")
    void createSingleConsentWithDecoupledFlowAndBlankCallbackUrl(String callbackUrl) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(callbackUrl)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Callback/webhook URL must not be blank");
    }

    @Test
    @DisplayName("Verify that null request is handled")
    void createSingleConsentWithGatewayFlowAndNullRequest() {
        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(null).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Single consent request must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow is handled")
    void createSingleConsentWithGatewayFlowAndNullAuthorisationFlow() {
        SingleConsentRequest request = new SingleConsentRequest()
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow detail is handled")
    void createSingleConsentWithGatewayFlowAndNullAuthorisationFlowDetail() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow())
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow detail must not be null");
    }

    @Test
    @DisplayName("Verify that null bank is handled")
    void createSingleConsentWithGatewayFlowAndNullBank() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint())))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Bank must not be null");
    }

    @Test
    @DisplayName("Verify that null amount is handled")
    void createSingleConsentWithGatewayFlowAndNullAmount() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Amount must not be null");
    }

    @Test
    @DisplayName("Verify that null currency is handled")
    void createSingleConsentWithGatewayFlowAndNullCurrency() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Currency must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createSingleConsentWithGatewayFlowAndInvalidTotal(String total) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total(total))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessageStartingWith("Validation failed for single consent request");
    }

    @Test
    @DisplayName("Verify that null PCR is handled")
    void createSingleConsentWithGatewayFlowAndNullPcr() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Verify that blank particulars is handled")
    void createSingleConsentWithGatewayFlowAndBlankParticulars(String particulars) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars(particulars)
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @Test
    @DisplayName("Verify that long PCR values are handled")
    void createSingleConsentWithGatewayFlowAndLongPcrValues() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("merchant particulars")
                        .code("merchant code")
                        .reference("merchant reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not exceed 12 characters");
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow and null flow hint is created")
    void createSingleConsentWithGatewayFlowAndNullFlowHint() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        CreateConsentResponse response = new CreateConsentResponse()
                .consentId(consentId);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(SINGLE_CONSENTS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(SingleConsentRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(consentId, null);
    }

    @Test
    @DisplayName("Verify that null authorisation flow hint type is handled")
    void createSingleConsentWithGatewayFlowAndNullFlowHintType() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new FlowHint()
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Flow hint type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank redirect URI is handled")
    void createSingleConsentWithGatewayFlowAndBlankRedirectUri(String redirectUri) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(redirectUri)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that gateway flow with null identifier type is handled")
    void createSingleConsentWithGatewayFlowAndNullIdentifierType() {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new DecoupledFlowHint()
                                        .identifierValue("+6449144425")
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank identifier value is handled")
    void createSingleConsentWithGatewayFlowAndBlankIdentifierValue(String identifierValue) {
        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new DecoupledFlowHint()
                                        .identifierType(IdentifierType.PHONE_NUMBER)
                                        .identifierValue(identifierValue)
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createSingleConsent(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @Test
    @DisplayName("Verify that null consent ID is handled")
    void getSingleConsentWithNullConsentId() {
        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.getSingleConsent(null).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Consent ID must not be null");
    }

    @Test
    @DisplayName("Verify that single consent is retrieved")
    void getSingleConsent() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        Consent consent = new Consent()
                .consentId(consentId)
                .status(Consent.StatusEnum.AWAITINGAUTHORISATION)
                .creationTimestamp(OffsetDateTime.now(ZoneId.of("Pacific/Auckland")).minusHours(1))
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
                .payments(Collections.emptySet());

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(consent));

        Mono<Consent> consentMono = client.getSingleConsent(consentId);

        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getAccounts, Consent::getPayments)
                .containsExactly(Consent.StatusEnum.AWAITINGAUTHORISATION, null, Collections.emptySet());
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
    @DisplayName("Verify that null consent ID is handled")
    void revokeSingleConsentWithNullConsentId() {
        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.revokeSingleConsent(null).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Consent ID must not be null");
    }

    @Test
    @DisplayName("Verify that single consent is revoked")
    void revokeSingleConsent() {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.empty());

        assertThatNoException().isThrownBy(() -> client.revokeSingleConsent(consentId).block());
    }

    @Test
    @DisplayName("Verify that single consent with redirect flow is created")
    void createSingleConsentWithRedirectFlow() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        CreateConsentResponse response = new CreateConsentResponse()
                .consentId(consentId)
                .redirectUri(REDIRECT_URI);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(SINGLE_CONSENTS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(SingleConsentRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(REDIRECT_URI)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(consentId, REDIRECT_URI);
    }

    @Test
    @DisplayName("Verify that single consent with decoupled flow is created")
    void createSingleConsentWithDecoupledFlow() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        CreateConsentResponse response = new CreateConsentResponse()
                .consentId(consentId);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(SINGLE_CONSENTS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(SingleConsentRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+6449144425")
                                .callbackUrl(CALLBACK_URL)))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(consentId, null);
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow and redirect flow hint is created")
    void createSingleConsentWithGatewayFlowAndRedirectFlowHint() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        CreateConsentResponse response = new CreateConsentResponse()
                .consentId(consentId);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(SINGLE_CONSENTS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(SingleConsentRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(consentId, null);
    }

    @Test
    @DisplayName("Verify that single consent with gateway flow and decoupled flow hint is created")
    void createSingleConsentWithGatewayFlowAndDecoupledFlowHint() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        CreateConsentResponse response = new CreateConsentResponse()
                .consentId(consentId);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(SINGLE_CONSENTS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(SingleConsentRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        SingleConsentRequest request = new SingleConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new DecoupledFlowHint()
                                        .identifierType(IdentifierType.PHONE_NUMBER)
                                        .identifierValue("+6449144425")
                                        .bank(Bank.PNZ))))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1.25"))
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"));

        Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(consentId, null);
    }
}
