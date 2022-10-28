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

import nz.co.blink.debit.dto.v1.AccountNumberRefundRequest;
import nz.co.blink.debit.dto.v1.FullRefundRequest;
import nz.co.blink.debit.dto.v1.PartialRefundRequest;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.Refund;
import nz.co.blink.debit.dto.v1.RefundDetail;
import nz.co.blink.debit.dto.v1.RefundResponse;
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

import static nz.co.blink.debit.enums.BlinkDebitConstant.REFUNDS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The client for refunds.
 */
@Component
public class RefundsApiClient {

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
    public RefundsApiClient(@Qualifier("blinkDebitClientHttpConnector") ReactorClientHttpConnector connector,
                            @Value("${blinkpay.debit.url:}") final String debitUrl,
                            AccessTokenHandler accessTokenHandler) {
        this.connector = connector;
        this.debitUrl = debitUrl;
        this.accessTokenHandler = accessTokenHandler;
    }

    /**
     * Creates a full refund.
     *
     * @param request the {@link FullRefundRequest}
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createFullRefund(FullRefundRequest request) {
        return createFullRefund(request, null);
    }

    /**
     * Creates a full refund.
     *
     * @param request   the {@link FullRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createFullRefund(FullRefundRequest request, final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Full refund request must not be null");
        }

        if (request.getPaymentId() == null) {
            throw new IllegalArgumentException("Payment ID must not be null");
        }

        Pcr pcr = request.getPcr();
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

        return createRefund(request, requestId);
    }

    /**
     * Creates a partial refund.
     *
     * @param request the {@link PartialRefundRequest}
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createPartialRefund(PartialRefundRequest request) {
        return createPartialRefund(request, null);
    }

    /**
     * Creates a partial refund.
     *
     * @param request   the {@link PartialRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createPartialRefund(PartialRefundRequest request, final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Partial refund request must not be null");
        }

        if (request.getPaymentId() == null) {
            throw new IllegalArgumentException("Payment ID must not be null");
        }

        Pcr pcr = request.getPcr();
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

        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }

        String total = request.getAmount().getTotal();
        if (StringUtils.isBlank(total) || !NumberUtils.isParsable(total)) {
            throw new IllegalArgumentException("Total is not a valid amount");
        }

        if (request.getAmount().getCurrency() == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }

        return createRefund(request, requestId);
    }

    /**
     * Creates an account number refund.
     *
     * @param request the {@link AccountNumberRefundRequest}
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createAccountNumberRefund(AccountNumberRefundRequest request) {
        return createAccountNumberRefund(request, null);
    }

    /**
     * Creates an account number refund.
     *
     * @param request   the {@link AccountNumberRefundRequest}
     * @param requestId the optional correlation ID
     * @return the {@link RefundResponse} {@link Mono}
     */
    public Mono<RefundResponse> createAccountNumberRefund(AccountNumberRefundRequest request, final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Account number refund request must not be null");
        }

        if (request.getPaymentId() == null) {
            throw new IllegalArgumentException("Payment ID must not be null");
        }

        return createRefund(request, requestId);
    }

    /**
     * Retrieves an existing refund by ID.
     *
     * @param refundId the refund ID
     * @return the {@link Payment} {@link Mono}
     */
    public Mono<Refund> getRefund(UUID refundId) {
        return getRefund(refundId, null);
    }

    /**
     * Retrieves an existing refund by ID.
     *
     * @param refundId  the refund ID
     * @param requestId the optional correlation ID
     * @return the {@link Payment} {@link Mono}
     */
    public Mono<Refund> getRefund(UUID refundId, final String requestId) {
        if (refundId == null) {
            throw new IllegalArgumentException("Refund ID must not be null");
        }

        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(REFUNDS_PATH.getValue() + "/{refundId}")
                        .build(refundId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add(REQUEST_ID.getValue(), correlationId))
                .exchangeToMono(ResponseHandler.getResponseMono(Refund.class));
    }

    private Mono<RefundResponse> createRefund(RefundDetail request, String requestId) {
        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .post()
                .uri(REFUNDS_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add(REQUEST_ID.getValue(), correlationId))
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.getResponseMono(RefundResponse.class));
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
