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

import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
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

import static nz.co.blink.debit.enums.BlinkDebitConstant.PAYMENTS_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link PaymentsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PaymentsApiClientTest {

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
    private PaymentsApiClient client;

    @Test
    @DisplayName("Verify that null consent ID is handled")
    void createPaymentWithNullConsentId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createPayment(null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Consent ID must not be null");
    }

    @Test
    @DisplayName("Verify that null account reference ID is handled")
    void createPaymentWithNullAccountReferenceId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createPayment(UUID.randomUUID(),
                (UUID) null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Account reference ID must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createEnduringPaymentWithInvalidTotal(String total) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createPayment(UUID.randomUUID(),
                "particulars", "code", "reference", total).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Total is not a valid amount");
    }

    @Test
    @DisplayName("Verify that payment is created")
    void createPayment() {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID paymentId = UUID.randomUUID();
        PaymentResponse response = new PaymentResponse()
                .paymentId(paymentId);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(PAYMENTS_PATH.getValue())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(PaymentRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(response));

        Mono<PaymentResponse> paymentResponseMono = client.createPayment(UUID.randomUUID());

        assertThat(paymentResponseMono).isNotNull();
        PaymentResponse actual = paymentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(PaymentResponse::getPaymentId)
                .isEqualTo(paymentId);
    }

    @Test
    @DisplayName("Verify that null refund ID is handled")
    void getPaymentWithNullPaymentId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.getPayment(null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Payment ID must not be null");
    }

    @Test
    @DisplayName("Verify that payment is retrieved")
    void getPayment() {
        ReflectionTestUtils.setField(client, "webClientBuilder", webClientBuilder);
        ReflectionTestUtils.setField(client, "debitUrl", "http://localhost:8080");

        UUID consentId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Pacific/Auckland"));
        Payment payment = new Payment()
                .paymentId(paymentId)
                .type(Payment.TypeEnum.SINGLE)
                .status(Payment.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED)
                .creationTimestamp(now)
                .statusUpdatedTimestamp(now.plusMinutes(5))
                .refunds(Collections.emptyList())
                .detail(new PaymentRequest()
                        .consentId(consentId));

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just(payment));

        Mono<Payment> paymentMono = client.getPayment(paymentId);

        assertThat(paymentMono).isNotNull();
        Payment actual = paymentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Payment::getPaymentId, Payment::getType, Payment::getStatus, Payment::getRefunds)
                .containsExactly(paymentId, Payment.TypeEnum.SINGLE, Payment.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED,
                        Collections.emptyList());
        assertThat(actual.getCreationTimestamp()).isEqualTo(now);
        assertThat(actual.getStatusUpdatedTimestamp()).isEqualTo(now.plusMinutes(5));
        PaymentRequest paymentRequest = actual.getDetail();
        assertThat(paymentRequest)
                .isNotNull()
                .extracting(PaymentRequest::getConsentId, PaymentRequest::getAccountReferenceId,
                        PaymentRequest::getEnduringPayment)
                .containsExactly(consentId, null, null);
    }
}
