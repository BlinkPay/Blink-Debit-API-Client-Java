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

import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.GatewayConsentRequest;
import nz.co.blink.debit.dto.v1.GatewayConsentResponse;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
import nz.co.blink.debit.helpers.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static nz.co.blink.debit.enums.BlinkDebitConstant.BEARER;
import static nz.co.blink.debit.enums.BlinkDebitConstant.GATEWAY_CONSENTS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.GATEWAY_QUICK_PAYMENTS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.INTERACTION_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The client for Blink gateway.
 */
@Component
public class GatewayApiClient {

    private final WebClient webClient;

    /**
     * Default constructor.
     *
     * @param webClient the {@link WebClient}
     */
    @Autowired
    public GatewayApiClient(@Qualifier("blinkDebitWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Returns a consent with the given ID only if type is gateway and consent was updated in the last 15 minutes.
     *
     * @param requestId    the correlation ID
     * @param sharedSecret the BFF shared secret
     * @param consentId    the consent ID
     * @return the {@link GatewayConsentResponse} {@link Mono}
     */
    Mono<GatewayConsentResponse> getConsent(final String requestId, final String sharedSecret, UUID consentId) {
        String authorization = BEARER.getValue() + sharedSecret;

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(GATEWAY_CONSENTS_PATH.getValue() + "/{consentId}")
                        .build(consentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);
                })
                .exchangeToMono(ResponseHandler.getResponseMono(GatewayConsentResponse.class));
    }

    /**
     * Returns a quick payment response with the given ID only if type is gateway,
     * payment was updated in the last 15 minutes, and it's a quick payment.
     *
     * @param requestId      the correlation ID
     * @param sharedSecret   the BFF shared secret
     * @param quickPaymentId the quick payment ID
     * @return the {@link GatewayConsentResponse} {@link Mono}
     */
    Mono<QuickPaymentResponse> getQuickPayment(final String requestId, final String sharedSecret, UUID quickPaymentId) {
        String authorization = BEARER.getValue() + sharedSecret;

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(GATEWAY_QUICK_PAYMENTS_PATH.getValue() + "/{quickPaymentId}")
                        .build(quickPaymentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);
                })
                .exchangeToMono(ResponseHandler.getResponseMono(QuickPaymentResponse.class));
    }

    /**
     * Updates a consent with the given ID.
     *
     * @param requestId               the correlation ID
     * @param sharedSecret            the BFF shared secret
     * @param consentId               the consent ID
     * @param bank                    the {@link Bank}
     * @param flow                    the {@link GatewayConsentRequest.FlowEnum}
     * @param identifierType          the {@link IdentifierType}
     * @param identifierValue         the identifier value
     * @param westpacAccountReference the optional Westpac account reference
     * @return the {@link GatewayConsentResponse} {@link Mono}
     */
    Mono<GatewayConsentResponse> updateConsent(final String requestId, final String sharedSecret, UUID consentId,
                                               Bank bank, GatewayConsentRequest.FlowEnum flow,
                                               IdentifierType identifierType, final String identifierValue,
                                               UUID westpacAccountReference) {
        GatewayConsentRequest request = new GatewayConsentRequest()
                .flow(flow)
                .bank(bank);

        if (GatewayConsentRequest.FlowEnum.DECOUPLED == flow) {
            request.identifierType(identifierType)
                    .identifierValue(identifierValue);
        }

        if (Bank.WESTPAC == bank) {
            request.westpacAccountRef(westpacAccountReference.toString());
        }

        String authorization = BEARER.getValue() + sharedSecret;

        return webClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(GATEWAY_CONSENTS_PATH.getValue() + "/{consentId}")
                        .build(consentId))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);
                    httpHeaders.add(INTERACTION_ID.getValue(), requestId);
                })
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.getResponseMono(GatewayConsentResponse.class));
    }
}
