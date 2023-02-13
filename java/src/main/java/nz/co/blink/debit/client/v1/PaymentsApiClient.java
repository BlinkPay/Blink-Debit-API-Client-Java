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

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import nz.co.blink.debit.config.BlinkPayProperties;
import nz.co.blink.debit.dto.v1.EnduringPaymentRequest;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.enums.BlinkDebitConstant;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import nz.co.blink.debit.helpers.ResponseHandler;
import nz.co.blink.debit.service.ValidationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
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
@Slf4j
public class PaymentsApiClient {

    private final ReactorClientHttpConnector connector;

    private final String debitUrl;

    private final AccessTokenHandler accessTokenHandler;

    private final ValidationService validationService;

    private final Retry retry;

    private WebClient.Builder webClientBuilder;

    /**
     * Default constructor.
     *
     * @param connector          the {@link ReactorClientHttpConnector}
     * @param properties         the {@link BlinkPayProperties}
     * @param accessTokenHandler the {@link AccessTokenHandler}
     * @param validationService  the {@link ValidationService}
     * @param retry              the {@link Retry} instance
     */
    @Autowired
    public PaymentsApiClient(@Qualifier("blinkDebitClientHttpConnector") ReactorClientHttpConnector connector,
                             BlinkPayProperties properties, AccessTokenHandler accessTokenHandler,
                             ValidationService validationService, Retry retry) {
        this.connector = connector;
        debitUrl = properties.getDebit().getUrl();
        this.accessTokenHandler = accessTokenHandler;
        this.validationService = validationService;
        this.retry = retry;
    }

    /**
     * Creates a payment.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<PaymentResponse> createPayment(PaymentRequest request) throws BlinkServiceException {
        return createPayment(request, null);
    }

    /**
     * Creates a payment.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<PaymentResponse> createPayment(PaymentRequest request, final String requestId)
            throws BlinkServiceException {
        if (request == null) {
            throw new BlinkInvalidValueException("Payment request must not be null");
        }

        if (request.getConsentId() == null) {
            throw new BlinkInvalidValueException("Consent ID must not be null");
        }

        EnduringPaymentRequest enduringPayment = request.getEnduringPayment();
        if (enduringPayment != null) {
            Pcr pcr = enduringPayment.getPcr();
            if (pcr == null) {
                throw new BlinkInvalidValueException("PCR must not be null");
            }

            if (StringUtils.isBlank(pcr.getParticulars())) {
                throw new BlinkInvalidValueException("Particulars must have at least 1 character");
            }

            if (StringUtils.length(pcr.getParticulars()) > 12
                    || StringUtils.length(pcr.getCode()) > 12
                    || StringUtils.length(pcr.getReference()) > 12) {
                throw new BlinkInvalidValueException("PCR must not exceed 12 characters");
            }

            if (enduringPayment.getAmount() == null) {
                throw new BlinkInvalidValueException("Amount must not be null");
            }

            if (enduringPayment.getAmount().getCurrency() == null) {
                throw new BlinkInvalidValueException("Currency must not be null");
            }
        }

        validationService.validateRequest("payment", request);

        return createPaymentMono(request, requestId);
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createPayment(PaymentRequest)}.
     *
     * @param request the {@link PaymentRequest}
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<PaymentResponse> createWestpacPayment(PaymentRequest request) throws BlinkServiceException {
        return createWestpacPayment(request, null);
    }

    /**
     * Creates a Westpac payment. Once Westpac enables their Open Banking API, this can be replaced with
     * {@link #createPayment(PaymentRequest, String)}.
     *
     * @param request   the {@link PaymentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<PaymentResponse> createWestpacPayment(PaymentRequest request, final String requestId)
            throws BlinkServiceException {
        if (request == null) {
            throw new BlinkInvalidValueException("Payment request must not be null");
        }

        if (request.getConsentId() == null) {
            throw new BlinkInvalidValueException("Consent ID must not be null");
        }

        if (request.getAccountReferenceId() == null) {
            throw new BlinkInvalidValueException("Account reference ID must not be null");
        }

        validationService.validateRequest("Westpac payment", request);

        return createPaymentMono(request, requestId);
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param paymentId the payment ID
     * @return the {@link Payment} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<Payment> getPayment(UUID paymentId) throws BlinkServiceException {
        return getPayment(paymentId, null);
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param paymentId the payment ID
     * @param requestId the optional correlation ID
     * @return the {@link Payment} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<Payment> getPayment(UUID paymentId, final String requestId) throws BlinkServiceException {
        if (paymentId == null) {
            throw new BlinkInvalidValueException("Payment ID must not be null");
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
                .exchangeToMono(ResponseHandler.handleResponseMono(Payment.class));
    }

    private Mono<PaymentResponse> createPaymentMono(PaymentRequest request, String requestId)
            throws BlinkServiceException {
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
                .exchangeToMono(ResponseHandler.handleResponseMono(PaymentResponse.class))
                .transformDeferred(RetryOperator.of(retry));
    }

    private WebClient.Builder getWebClientBuilder(String requestId) throws BlinkServiceException {
        if (webClientBuilder != null) {
            return webClientBuilder;
        }

        return WebClient.builder()
                .clientConnector(connector)
                .defaultHeader(HttpHeaders.USER_AGENT, BlinkDebitConstant.USER_AGENT_VALUE.getValue())
                .baseUrl(debitUrl)
                .filter(accessTokenHandler.setAccessToken(requestId));
    }
}
