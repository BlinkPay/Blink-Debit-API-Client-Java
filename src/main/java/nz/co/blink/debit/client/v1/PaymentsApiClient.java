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
import nz.co.blink.debit.dto.v1.EnduringPaymentRequest;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import nz.co.blink.debit.helpers.ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static nz.co.blink.debit.enums.BlinkDebitConstant.INTERACTION_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.PAYMENTS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The client for payments.
 */
@Component
public class PaymentsApiClient {

    private final ReactorClientHttpConnector connector;

    private final String debitUrl;

    private final AccessTokenHandler accessTokenHandler;

    private WebClient.Builder webClientBuilder;

    /**
     * Default constructor.
     *
     * @param connector          the {@link ReactorClientHttpConnector}
     * @param debitUrl           the Blink Debit URL
     * @param accessTokenHandler the {@link AccessTokenHandler}
     */
    @Autowired
    public PaymentsApiClient(@Qualifier("blinkDebitClientHttpConnector") ReactorClientHttpConnector connector,
                             @Value("${blinkpay.debit.url:}") final String debitUrl,
                             AccessTokenHandler accessTokenHandler) {
        this.connector = connector;
        this.debitUrl = debitUrl;
        this.accessTokenHandler = accessTokenHandler;
    }

    /**
     * Creates a payment for a single consent.
     *
     * @param consentId the required consent ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPayment(UUID consentId) {
        return createPayment(consentId, (String) null);
    }

    /**
     * Creates a payment for a single consent.
     *
     * @param consentId the required consent ID
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPayment(UUID consentId, final String requestId) {
        return createPayment(consentId, null, null, null, null, null, requestId);
    }

    /**
     * Creates a payment for an enduring consent.
     *
     * @param consentId          the required consent ID
     * @param particulars        the optional particulars of the enduring payment request
     * @param code               the optional code of the enduring payment request
     * @param reference          the optional reference of the enduring payment request
     * @param total              the total of the enduring payment request
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPayment(UUID consentId, final String particulars, final String code,
                                               final String reference, final String total) {
        return createPayment(consentId, particulars, code, reference, total, null);
    }

    /**
     * Creates a payment for an enduring consent.
     *
     * @param consentId          the required consent ID
     * @param particulars        the optional particulars of the enduring payment request
     * @param code               the optional code of the enduring payment request
     * @param reference          the optional reference of the enduring payment request
     * @param total              the total of the enduring payment request
     * @param requestId          the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPayment(UUID consentId, final String particulars, final String code,
                                               final String reference, final String total, final String requestId) {
        return createPayment(consentId, null, particulars, code, reference, total, requestId);
    }

    /**
     * Creates a Westpac payment.
     *
     * @param consentId          the required consent ID
     * @param accountReferenceId the Westpac account reference ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPayment(UUID consentId, UUID accountReferenceId) {
        return createPayment(consentId, accountReferenceId, null);
    }

    /**
     * Creates a Westpac payment.
     *
     * @param consentId          the required consent ID
     * @param accountReferenceId the Westpac account reference ID
     * @param requestId          the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPayment(UUID consentId, UUID accountReferenceId, final String requestId) {
        if (accountReferenceId == null) {
            throw new IllegalArgumentException("Account reference ID must not be null");
        }

        return createPayment(consentId, accountReferenceId, null, null, null, null, requestId);
    }

    /**
     * Creates a payment for single or enduring consent.
     *
     * @param consentId          the required consent ID
     * @param accountReferenceId the optional Westpac account reference ID
     * @param particulars        the optional particulars of the enduring payment request
     * @param code               the optional code of the enduring payment request
     * @param reference          the optional reference of the enduring payment request
     * @param total              the total of the enduring payment request
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPayment(UUID consentId, UUID accountReferenceId, final String particulars,
                                               final String code, final String reference, final String total) {
        return createPayment(consentId, accountReferenceId, particulars, code, reference, total, null);
    }

    /**
     * Creates a payment for single or enduring consent.
     *
     * @param consentId          the required consent ID
     * @param accountReferenceId the optional Westpac account reference ID
     * @param particulars        the optional particulars of the enduring payment request
     * @param code               the optional code of the enduring payment request
     * @param reference          the optional reference of the enduring payment request
     * @param total              the total of the enduring payment request
     * @param requestId          the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createPayment(UUID consentId, UUID accountReferenceId, final String particulars,
                                               final String code, final String reference, final String total,
                                               final String requestId) {
        if (consentId == null) {
            throw new IllegalArgumentException("Consent ID must not be null");
        }

        EnduringPaymentRequest enduringPaymentRequest = null;
        if (StringUtils.isNotEmpty(particulars)) {
            if (!NumberUtils.isParsable(total)) {
                throw new IllegalArgumentException("Total is not a valid amount");
            }
            enduringPaymentRequest = new EnduringPaymentRequest()
                    .pcr(new Pcr()
                            .particulars(StringUtils.truncate(particulars, 12))
                            .code(StringUtils.truncate(code, 12))
                            .reference(StringUtils.truncate(reference, 12)))
                    .amount(new Amount()
                            .currency(Amount.CurrencyEnum.NZD)
                            .total(total));
        }

        PaymentRequest request = new PaymentRequest()
                .consentId(consentId)
                .accountReferenceId(accountReferenceId)
                .enduringPayment(enduringPaymentRequest);

        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .post()
                .uri(PAYMENTS_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), correlationId);
                    httpHeaders.add(INTERACTION_ID.getValue(), correlationId);
                })
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.getResponseMono(PaymentResponse.class));
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param paymentId the payment ID
     * @return the {@link Payment} {@link Mono}
     */
    public Mono<Payment> getPayment(UUID paymentId) {
        return getPayment(paymentId, null);
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param paymentId the payment ID
     * @param requestId the optional correlation ID
     * @return the {@link Payment} {@link Mono}
     */
    public Mono<Payment> getPayment(UUID paymentId, final String requestId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID must not be null");
        }

        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PAYMENTS_PATH.getValue() + "/{paymentId}")
                        .build(paymentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), correlationId);
                    httpHeaders.add(INTERACTION_ID.getValue(), correlationId);
                })
                .exchangeToMono(ResponseHandler.getResponseMono(Payment.class));
    }

    private WebClient.Builder getWebClientBuilder(String correlationId) {
        if (webClientBuilder != null) {
            return webClientBuilder;
        }

        return WebClient.builder()
                .clientConnector(connector)
                .baseUrl(debitUrl)
                .filter(accessTokenHandler.setAccessToken(correlationId));
    }
}
