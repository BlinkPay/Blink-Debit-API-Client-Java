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
import nz.co.blink.debit.dto.v1.AccountNumberRefundRequest;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.FullRefundRequest;
import nz.co.blink.debit.dto.v1.PartialRefundRequest;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.Refund;
import nz.co.blink.debit.dto.v1.RefundDetail;
import nz.co.blink.debit.dto.v1.RefundResponse;
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
import static nz.co.blink.debit.enums.BlinkDebitConstant.REFUNDS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The client for refunds.
 */
@Component
public class RefundsApiClient {

    private final WebClient webClient;

    /**
     * Default constructor.
     *
     * @param webClient the {@link WebClient}
     */
    @Autowired
    public RefundsApiClient(@Qualifier("blinkDebitWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Creates an account number refund.
     *
     * @param requestId   the correlation ID
     * @param accessToken the OAuth2 access token
     * @param type        the {@link RefundDetail.TypeEnum}
     * @param paymentId   the payment ID
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Mono<RefundResponse> createRefund(final String requestId, final String accessToken,
                                             RefundDetail.TypeEnum type, UUID paymentId)
            throws ExpiredAccessTokenException {
        return createRefund(requestId, accessToken, type, paymentId, null, null, null, null, null);
    }

    /**
     * Creates a full refund.
     *
     * @param requestId   the correlation ID
     * @param accessToken the OAuth2 access token
     * @param type        the {@link RefundDetail.TypeEnum}
     * @param paymentId   the payment ID
     * @param redirectUri the redirect URI
     * @param particulars the particulars
     * @param code        the code
     * @param reference   the reference
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Mono<RefundResponse> createRefund(final String requestId, final String accessToken,
                                             RefundDetail.TypeEnum type, UUID paymentId, final String redirectUri,
                                             final String particulars, final String code, final String reference)
            throws ExpiredAccessTokenException {
        return createRefund(requestId, accessToken, type, paymentId, redirectUri, particulars, code, reference, null);
    }

    /**
     * Creates a refund.
     *
     * @param requestId   the correlation ID
     * @param accessToken the OAuth2 access token
     * @param type        the {@link RefundDetail.TypeEnum}
     * @param paymentId   the payment ID
     * @param redirectUri the redirect URI
     * @param particulars the particulars
     * @param code        the code
     * @param reference   the reference
     * @param total       the total for partial refund
     * @return the {@link PaymentResponse} {@link Mono}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Mono<RefundResponse> createRefund(final String requestId, final String accessToken,
                                             RefundDetail.TypeEnum type, UUID paymentId, final String redirectUri,
                                             final String particulars, final String code, final String reference,
                                             final String total)
            throws ExpiredAccessTokenException {
        if (StringUtils.isBlank(requestId)) {
            throw new IllegalArgumentException("Request ID must not be blank");
        }

        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID must not be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Refund type must not be null");
        }
        RefundDetail request = null;
        switch (type) {
            case FULL_REFUND:
                if (StringUtils.isEmpty(particulars)) {
                    throw new IllegalArgumentException("Particulars must have at least 1 character");
                }
                if (StringUtils.isBlank(redirectUri)) {
                    throw new IllegalArgumentException("Redirect URI must not be blank");
                }
                request = new FullRefundRequest()
                        .pcr(new Pcr()
                                .particulars(StringUtils.truncate(particulars, 20))
                                .code(StringUtils.truncate(code, 20))
                                .reference(StringUtils.truncate(reference, 20)))
                        .consentRedirect(redirectUri)
                        .paymentId(paymentId)
                        .type(type);
                break;
            case PARTIAL_REFUND:
                if (StringUtils.isEmpty(particulars)) {
                    throw new IllegalArgumentException("Particulars must have at least 1 character");
                }
                if (StringUtils.isBlank(redirectUri)) {
                    throw new IllegalArgumentException("Redirect URI must not be blank");
                }
                if (!NumberUtils.isParsable(total)) {
                    throw new IllegalArgumentException("Total is not a valid amount");
                }
                request = new PartialRefundRequest()
                        .pcr(new Pcr()
                                .particulars(StringUtils.truncate(particulars, 20))
                                .code(StringUtils.truncate(code, 20))
                                .reference(StringUtils.truncate(reference, 20)))
                        .consentRedirect(redirectUri)
                        .amount(new Amount()
                                .currency(Amount.CurrencyEnum.NZD)
                                .total(total))
                        .paymentId(paymentId)
                        .type(type);
                break;
            case ACCOUNT_NUMBER:
                request = new AccountNumberRefundRequest()
                        .paymentId(paymentId)
                        .type(type);
                break;
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
                .post()
                .uri(REFUNDS_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);
                })
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.getResponseMono(RefundResponse.class));
    }

    /**
     * Retrieves an existing refund by ID.
     *
     * @param requestId   the correlation ID
     * @param accessToken the OAuth2 access token
     * @param refundId    the refund ID
     * @return the {@link Payment} {@link Mono}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Mono<Refund> getRefund(final String requestId, final String accessToken, UUID refundId)
            throws ExpiredAccessTokenException {
        if (StringUtils.isBlank(requestId)) {
            throw new IllegalArgumentException("Request ID must not be blank");
        }

        if (refundId == null) {
            throw new IllegalArgumentException("Refund ID must not be null");
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
                        .path(REFUNDS_PATH.getValue() + "/{refundId}")
                        .build(refundId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);
                })
                .exchangeToMono(ResponseHandler.getResponseMono(Refund.class));
    }
}
