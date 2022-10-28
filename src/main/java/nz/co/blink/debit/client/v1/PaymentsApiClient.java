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
     * Creates a single payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createSinglePayment(PaymentRequest request) {
        return createSinglePayment(request, null);
    }

    /**
     * Creates a single payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createSinglePayment(PaymentRequest request, final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request must not be null");
        }

        if (request.getConsentId() == null) {
            throw new IllegalArgumentException("Consent ID must not be null");
        }

        return createPayment(request, requestId);
    }

    /**
     * Creates an enduring payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createEnduringPayment(PaymentRequest request) {
        return createEnduringPayment(request, null);
    }

    /**
     * Creates an enduring payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createEnduringPayment(PaymentRequest request, final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request must not be null");
        }

        if (request.getConsentId() == null) {
            throw new IllegalArgumentException("Consent ID must not be null");
        }

        EnduringPaymentRequest enduringPayment = request.getEnduringPayment();
        if (enduringPayment == null) {
            throw new IllegalArgumentException("Enduring payment must not be null");
        }

        Pcr pcr = enduringPayment.getPcr();
        if (pcr == null) {
            throw new IllegalArgumentException("PCR must not be null");
        }

        if (StringUtils.isBlank(pcr.getParticulars())) {
            throw new IllegalArgumentException("Particulars must have at least 1 character");
        }

        if (StringUtils.length(pcr.getParticulars()) > 12
                || StringUtils.length(pcr.getCode()) > 12
                || StringUtils.length(pcr.getReference()) > 12) {
            throw new IllegalArgumentException("PCR must not exceed 12 characters");
        }

        if (enduringPayment.getAmount() == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }

        if (enduringPayment.getAmount().getCurrency() == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }

        String total = enduringPayment.getAmount().getTotal();
        if (StringUtils.isBlank(total) || !NumberUtils.isParsable(total)) {
            throw new IllegalArgumentException("Total is not a valid amount");
        }

        return createPayment(request, requestId);
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createSinglePayment(PaymentRequest)}.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createWestpacPayment(PaymentRequest request) {
        return createWestpacPayment(request, null);
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createSinglePayment(PaymentRequest, String)}.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     */
    public Mono<PaymentResponse> createWestpacPayment(PaymentRequest request, final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request must not be null");
        }

        if (request.getConsentId() == null) {
            throw new IllegalArgumentException("Consent ID must not be null");
        }

        if (request.getAccountReferenceId() == null) {
            throw new IllegalArgumentException("Account reference ID must not be null");
        }

        return createPayment(request, requestId);
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

    private Mono<PaymentResponse> createPayment(PaymentRequest request, String requestId) {
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

    private WebClient.Builder getWebClientBuilder(String requestId) {
        if (webClientBuilder != null) {
            return webClientBuilder;
        }

        return WebClient.builder()
                .clientConnector(connector)
                .baseUrl(debitUrl)
                .filter(accessTokenHandler.setAccessToken(requestId));
    }
}
