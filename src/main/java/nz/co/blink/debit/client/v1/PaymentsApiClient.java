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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.EnduringPaymentRequest;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.exception.ExpiredAccessTokenException;
import nz.co.blink.debit.helpers.ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

import static nz.co.blink.debit.enums.BlinkDebitConstant.BEARER;
import static nz.co.blink.debit.enums.BlinkDebitConstant.INTERACTION_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.PAYMENTS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The client for payments.
 */
@Component
public class PaymentsApiClient {

    private final WebClient webClient;

    /**
     * Default constructor.
     *
     * @param webClient the {@link WebClient}
     */
    @Autowired
    public PaymentsApiClient(@Qualifier("blinkDebitWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Creates a payment for a single consent.
     *
     * @param requestId   the correlation ID
     * @param accessToken the OAuth2 access token
     * @param consentId   the required consent ID
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Mono<PaymentResponse> createPayment(final String requestId, final String accessToken, UUID consentId)
            throws ExpiredAccessTokenException {
        return createPayment(requestId, accessToken, consentId, null, null, null, null, null);
    }

    /**
     * Creates a Westpac payment.
     *
     * @param requestId          the correlation ID
     * @param accessToken        the OAuth2 access token
     * @param consentId          the required consent ID
     * @param accountReferenceId the Westpac account reference ID
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Mono<PaymentResponse> createPayment(final String requestId, final String accessToken, UUID consentId,
                                               UUID accountReferenceId)
            throws ExpiredAccessTokenException {
        if (accountReferenceId == null) {
            throw new IllegalArgumentException("Account reference ID must not be null");
        }

        return createPayment(requestId, accessToken, consentId, accountReferenceId, null, null, null, null);
    }

    /**
     * Creates a payment for single or enduring consent.
     *
     * @param requestId          the correlation ID
     * @param accessToken        the OAuth2 access token
     * @param consentId          the required consent ID
     * @param accountReferenceId the optional Westpac account reference ID
     * @param particulars        the optional particulars of the enduring payment request
     * @param code               the optional code of the enduring payment request
     * @param reference          the optional reference of the enduring payment request
     * @param total              the total of the enduring payment request
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Mono<PaymentResponse> createPayment(final String requestId, final String accessToken,
                                               UUID consentId, UUID accountReferenceId, final String particulars,
                                               final String code, final String reference, final String total)
            throws ExpiredAccessTokenException {
        if (StringUtils.isBlank(requestId)) {
            throw new IllegalArgumentException("Request ID must not be blank");
        }

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
                            .particulars(StringUtils.truncate(particulars, 20))
                            .code(StringUtils.truncate(code, 20))
                            .reference(StringUtils.truncate(reference, 20)))
                    .amount(new Amount()
                            .currency(Amount.CurrencyEnum.NZD)
                            .total(total));
        }

        PaymentRequest request = new PaymentRequest()
                .consentId(consentId)
                .accountReferenceId(accountReferenceId)
                .enduringPayment(enduringPaymentRequest);

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("Access token must not be blank");
        }
        DecodedJWT jwt = JWT.decode(accessToken);
        if (jwt.getExpiresAt().before(new Date())) {
            throw new ExpiredAccessTokenException();
        }

        String authorization = BEARER.getValue() + accessToken;

        return webClient
                .post()
                .uri(PAYMENTS_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);
                    httpHeaders.add(INTERACTION_ID.getValue(), requestId);
                })
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.getResponseMono(PaymentResponse.class));
    }

    /**
     * Retrieves an existing payment by ID.
     *
     * @param requestId   the correlation ID
     * @param accessToken the OAuth2 access token
     * @param paymentId   the payment ID
     * @return the {@link Payment} {@link Mono}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Mono<Payment> getPayment(final String requestId, final String accessToken, UUID paymentId)
            throws ExpiredAccessTokenException {
        if (StringUtils.isBlank(requestId)) {
            throw new IllegalArgumentException("Request ID must not be blank");
        }

        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID must not be null");
        }

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("Access token must not be blank");
        }
        DecodedJWT jwt = JWT.decode(accessToken);
        if (jwt.getExpiresAt().before(new Date())) {
            throw new ExpiredAccessTokenException();
        }

        String authorization = BEARER.getValue() + accessToken;

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(PAYMENTS_PATH.getValue() + "/{paymentId}")
                        .build(paymentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);
                    httpHeaders.add(INTERACTION_ID.getValue(), requestId);
                })
                .exchangeToMono(ResponseHandler.getResponseMono(Payment.class));
    }
}
