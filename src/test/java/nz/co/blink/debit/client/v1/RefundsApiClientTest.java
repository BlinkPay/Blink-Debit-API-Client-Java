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
import nz.co.blink.debit.dto.v1.AccountNumberRefundRequest;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.FullRefundRequest;
import nz.co.blink.debit.dto.v1.OneOfrefundRequest;
import nz.co.blink.debit.dto.v1.PartialRefundRequest;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.Refund;
import nz.co.blink.debit.dto.v1.RefundDetail;
import nz.co.blink.debit.dto.v1.RefundResponse;
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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static nz.co.blink.debit.enums.BlinkDebitConstant.REFUNDS_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link RefundsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class RefundsApiClientTest {

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

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Spy
    private Retry retry = Retry.ofDefaults("retry");

    @InjectMocks
    private RefundsApiClient client;

    @Test
    @DisplayName("Verify that null request is handled")
    void createAccountNumberRefundWithNullRequest() {
        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(null).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Refund request must not be null");
    }

    @Test
    @DisplayName("Verify that null payment ID is handled")
    void createAccountNumberRefundWithNullPaymentId() {
        AccountNumberRefundRequest request = new AccountNumberRefundRequest();

        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                client.createRefund(request).block(), BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Payment ID must not be null");
    }

    @Test
    @DisplayName("Verify that null request is handled")
    void createFullRefundWithNullRequest() {
        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(null).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Refund request must not be null");
    }

    @Test
    @DisplayName("Verify that null payment ID is handled")
    void createFullRefundWithNullPaymentId() {
        FullRefundRequest request = new FullRefundRequest();

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Payment ID must not be null");
    }

    @Test
    @DisplayName("Verify that null PCR is handled")
    void createFullRefundWithNullPcr() {
        FullRefundRequest request = (FullRefundRequest) new FullRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .paymentId(UUID.randomUUID());

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Verify that blank particulars is handled")
    void createFullRefundWithBlankParticulars(String particulars) {
        FullRefundRequest request = (FullRefundRequest) new FullRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars(particulars)
                        .code("code")
                        .reference("reference"))
                .paymentId(UUID.randomUUID());

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @Test
    @DisplayName("Verify that long PCR values are handled")
    void createFullRefundWithLongPcrValues() {
        FullRefundRequest request = (FullRefundRequest) new FullRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars("merchant particulars")
                        .code("merchant code")
                        .reference("merchant reference"))
                .paymentId(UUID.randomUUID());

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not exceed 12 characters");
    }

    @Test
    @DisplayName("Verify that null request is handled")
    void createPartialRefundWithNullRequest() {
        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(null).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Refund request must not be null");
    }

    @Test
    @DisplayName("Verify that null payment ID is handled")
    void createPartialRefundWithNullPaymentId() {
        PartialRefundRequest request = new PartialRefundRequest();

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Payment ID must not be null");
    }

    @Test
    @DisplayName("Verify that null PCR is handled")
    void createPartialRefundWithNullPcr() {
        PartialRefundRequest request = (PartialRefundRequest) new PartialRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .paymentId(UUID.randomUUID());

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("PCR must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Verify that blank particulars is handled")
    void createPartialRefundWithBlankParticulars(String particulars) {
        PartialRefundRequest request = (PartialRefundRequest) new PartialRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars(particulars)
                        .code("code")
                        .reference("reference"))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("25.50"))
                .paymentId(UUID.randomUUID());

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @Test
    @DisplayName("Verify that null amount is handled")
    void createPartialRefundWithNullAmount() {
        PartialRefundRequest request = (PartialRefundRequest) new PartialRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .paymentId(UUID.randomUUID());

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Amount must not be null");
    }

    @Test
    @DisplayName("Verify that null currency is handled")
    void createPartialRefundWithNullCurrency() {
        PartialRefundRequest request = (PartialRefundRequest) new PartialRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .amount(new Amount()
                        .total("25.50"))
                .paymentId(UUID.randomUUID());

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Currency must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createPartialRefundWithInvalidTotal(String total) {
        PartialRefundRequest request = (PartialRefundRequest) new PartialRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total(total))
                .paymentId(UUID.randomUUID());

        BlinkInvalidValueException exception = catchThrowableOfType(() -> client.createRefund(request).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessageStartingWith("Validation failed for refund request");
    }

    @Test
    @DisplayName("Verify that null refund ID is handled")
    void getRefundWithNullRefundId() {
        BlinkInvalidValueException exception = catchThrowableOfType(() ->
                        client.getRefund(null, UUID.randomUUID().toString()).block(),
                BlinkInvalidValueException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Refund ID must not be null");
    }

    @Test
    @DisplayName("Verify that refund is retrieved")
    void getRefund() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID paymentId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Pacific/Auckland"));
        Refund refund = new Refund()
                .refundId(refundId)
                .accountNumber("99-6121-6242460-00")
                .status(Refund.StatusEnum.COMPLETED)
                .creationTimestamp(now)
                .statusUpdatedTimestamp(now.plusMinutes(5))
                .detail((OneOfrefundRequest) new AccountNumberRefundRequest()
                        .paymentId(paymentId)
                        .type(RefundDetail.TypeEnum.ACCOUNT_NUMBER));

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(refund));

        Mono<Refund> refundMono = client.getRefund(refundId);

        assertThat(refundMono).isNotNull();
        Refund actual = refundMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Refund::getRefundId, Refund::getStatus, Refund::getAccountNumber)
                .containsExactly(refundId, Refund.StatusEnum.COMPLETED, "99-6121-6242460-00");
        assertThat(actual.getCreationTimestamp()).isEqualTo(now);
        assertThat(actual.getStatusUpdatedTimestamp()).isEqualTo(now.plusMinutes(5));
        RefundDetail refundDetail = (RefundDetail) actual.getDetail();
        assertThat(refundDetail)
                .isNotNull()
                .extracting(RefundDetail::getPaymentId, RefundDetail::getType)
                .containsExactly(paymentId, RefundDetail.TypeEnum.ACCOUNT_NUMBER);
    }

    @Test
    @DisplayName("Verify that account number refund is created")
    void createAccountNumberRefund() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID refundId = UUID.randomUUID();
        RefundResponse response = new RefundResponse()
                .refundId(refundId);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(REFUNDS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(RefundDetail.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        AccountNumberRefundRequest request = (AccountNumberRefundRequest) new AccountNumberRefundRequest()
                .paymentId(UUID.randomUUID());

        Mono<RefundResponse> refundResponseMono = client.createRefund(request);

        assertThat(refundResponseMono).isNotNull();
        RefundResponse actual = refundResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(RefundResponse::getRefundId)
                .isEqualTo(refundId);
    }

    @Test
    @DisplayName("Verify that full refund is created")
    void createFullRefund() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID refundId = UUID.randomUUID();
        RefundResponse response = new RefundResponse()
                .refundId(refundId);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(REFUNDS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(RefundDetail.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        FullRefundRequest request = (FullRefundRequest) new FullRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .paymentId(UUID.randomUUID());

        Mono<RefundResponse> refundResponseMono = client.createRefund(request);

        assertThat(refundResponseMono).isNotNull();
        RefundResponse actual = refundResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(RefundResponse::getRefundId)
                .isEqualTo(refundId);
    }

    @Test
    @DisplayName("Verify that partial refund is created")
    void createPartialRefund() throws BlinkInvalidValueException {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID refundId = UUID.randomUUID();
        RefundResponse response = new RefundResponse()
                .refundId(refundId);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(REFUNDS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(RefundDetail.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        PartialRefundRequest request = (PartialRefundRequest) new PartialRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("25.50"))
                .paymentId(UUID.randomUUID());

        Mono<RefundResponse> refundResponseMono = client.createRefund(request);

        assertThat(refundResponseMono).isNotNull();
        RefundResponse actual = refundResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(RefundResponse::getRefundId)
                .isEqualTo(refundId);
    }
}
