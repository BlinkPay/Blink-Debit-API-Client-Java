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
import nz.co.blink.debit.dto.v1.AuthFlow;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.OneOfauthFlowDetail;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
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
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.SINGLE_CONSENTS_PATH;

/**
 * The client for single consents.
 */
@Component
public class SingleConsentsApiClient {

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
    public SingleConsentsApiClient(@Qualifier("blinkDebitClientHttpConnector") ReactorClientHttpConnector connector,
                                   @Value("${blinkpay.debit.url:}") final String debitUrl,
                                   AccessTokenHandler accessTokenHandler) {
        this.connector = connector;
        this.debitUrl = debitUrl;
        this.accessTokenHandler = accessTokenHandler;
    }

    /**
     * Creates a single consent with redirect flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithRedirectFlow(SingleConsentRequest request) {
        return createSingleConsentWithRedirectFlow(request, null);
    }

    /**
     * Creates a single consent with redirect flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithRedirectFlow(SingleConsentRequest request,
                                                                           final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Single consent request must not be null");
        }

        AuthFlow flow = request.getFlow();
        if (flow == null) {
            throw new IllegalArgumentException("Authorisation flow must not be null");
        }

        OneOfauthFlowDetail detail = flow.getDetail();
        if (detail == null) {
            throw new IllegalArgumentException("Authorisation flow detail must not be null");
        }

        if (!(detail instanceof RedirectFlow)) {
            throw new IllegalArgumentException("Authorisation flow detail must be a RedirectFlow");
        }

        RedirectFlow redirectFlow = (RedirectFlow) flow.getDetail();
        if (redirectFlow.getBank() == null) {
            throw new IllegalArgumentException("Bank must not be null");
        }

        if (StringUtils.isBlank(redirectFlow.getRedirectUri())) {
            throw new IllegalArgumentException("Redirect URI must not be blank");
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

        Amount amount = request.getAmount();
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }

        if (amount.getCurrency() == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }

        String total = amount.getTotal();
        if (StringUtils.isBlank(total) || !NumberUtils.isParsable(total)) {
            throw new IllegalArgumentException("Total is not a valid amount");
        }

        return createSingleConsent(request, requestId);
    }

    /**
     * Creates a single consent with decoupled flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithDecoupledFlow(SingleConsentRequest request) {
        return createSingleConsentWithDecoupledFlow(request, null);
    }

    /**
     * Creates a single consent with decoupled flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithDecoupledFlow(SingleConsentRequest request,
                                                                            final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Single consent request must not be null");
        }

        AuthFlow flow = request.getFlow();
        if (flow == null) {
            throw new IllegalArgumentException("Authorisation flow must not be null");
        }

        OneOfauthFlowDetail detail = flow.getDetail();
        if (detail == null) {
            throw new IllegalArgumentException("Authorisation flow detail must not be null");
        }

        if (!(detail instanceof DecoupledFlow)) {
            throw new IllegalArgumentException("Authorisation flow detail must be a DecoupledFlow");
        }

        DecoupledFlow decoupledFlow = (DecoupledFlow) flow.getDetail();
        if (decoupledFlow.getBank() == null) {
            throw new IllegalArgumentException("Bank must not be null");
        }

        if (decoupledFlow.getIdentifierType() == null) {
            throw new IllegalArgumentException("Identifier type must not be null");
        }

        if (StringUtils.isBlank(decoupledFlow.getIdentifierValue())) {
            throw new IllegalArgumentException("Identifier value must not be blank");
        }

        if (StringUtils.isBlank(decoupledFlow.getCallbackUrl())) {
            throw new IllegalArgumentException("Callback/webhook URL must not be blank");
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

        Amount amount = request.getAmount();
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }

        if (amount.getCurrency() == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }

        String total = amount.getTotal();
        if (StringUtils.isBlank(total) || !NumberUtils.isParsable(total)) {
            throw new IllegalArgumentException("Total is not a valid amount");
        }

        return createSingleConsent(request, requestId);
    }

    /**
     * Creates a single consent with gateway flow.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithGatewayFlow(SingleConsentRequest request) {
        return createSingleConsentWithGatewayFlow(request, null);
    }

    /**
     * Creates a single consent with gateway flow.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createSingleConsentWithGatewayFlow(SingleConsentRequest request,
                                                                          final String requestId) {
        if (request == null) {
            throw new IllegalArgumentException("Single consent request must not be null");
        }

        AuthFlow flow = request.getFlow();
        if (flow == null) {
            throw new IllegalArgumentException("Authorisation flow must not be null");
        }

        OneOfauthFlowDetail detail = flow.getDetail();
        if (detail == null) {
            throw new IllegalArgumentException("Authorisation flow detail must not be null");
        }

        if (!(detail instanceof GatewayFlow)) {
            throw new IllegalArgumentException("Authorisation flow detail must be a GatewayFlow");
        }

        GatewayFlow gatewayFlow = (GatewayFlow) flow.getDetail();
        if (StringUtils.isBlank(gatewayFlow.getRedirectUri())) {
            throw new IllegalArgumentException("Redirect URI must not be blank");
        }

        FlowHint flowHint = gatewayFlow.getFlowHint();
        if (flowHint == null) {
            throw new IllegalArgumentException("Flow hint must not be null");
        }

        if (flowHint.getBank() == null) {
            throw new IllegalArgumentException("Bank must not be null");
        }

        FlowHint.TypeEnum flowHintType = flowHint.getType();
        if (flowHintType == null) {
            throw new IllegalArgumentException("Flow hint type must not be null");
        }

        if (FlowHint.TypeEnum.DECOUPLED == flowHintType) {
            DecoupledFlowHint decoupledFlowHint = (DecoupledFlowHint) flowHint;
            if (decoupledFlowHint.getIdentifierType() == null) {
                throw new IllegalArgumentException("Identifier type must not be null");
            }

            if (StringUtils.isBlank(decoupledFlowHint.getIdentifierValue())) {
                throw new IllegalArgumentException("Identifier value must not be blank");
            }
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

        Amount amount = request.getAmount();
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }

        if (amount.getCurrency() == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }

        String total = amount.getTotal();
        if (StringUtils.isBlank(total) || !NumberUtils.isParsable(total)) {
            throw new IllegalArgumentException("Total is not a valid amount");
        }

        return createSingleConsent(request, requestId);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @return the {@link Consent} {@link Mono}
     */
    public Mono<Consent> getSingleConsent(UUID consentId) {
        return getSingleConsent(consentId, null);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     * @return the {@link Consent} {@link Mono}
     */
    public Mono<Consent> getSingleConsent(UUID consentId, String requestId) {
        if (consentId == null) {
            throw new IllegalArgumentException("Consent ID must not be null");
        }

        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(SINGLE_CONSENTS_PATH.getValue() + "/{consentId}")
                        .build(consentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), correlationId);
                    httpHeaders.add(INTERACTION_ID.getValue(), correlationId);
                })
                .exchangeToMono(ResponseHandler.getResponseMono(Consent.class));
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     */
    public Mono<Void> revokeSingleConsent(UUID consentId) {
        return revokeSingleConsent(consentId, null);
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     */
    public Mono<Void> revokeSingleConsent(UUID consentId, final String requestId) {
        if (consentId == null) {
            throw new IllegalArgumentException("Consent ID must not be null");
        }

        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(SINGLE_CONSENTS_PATH.getValue() + "/{consentId}")
                        .build(consentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), correlationId);
                    httpHeaders.add(INTERACTION_ID.getValue(), correlationId);
                })
                .exchangeToMono(ResponseHandler.getResponseMono(Void.class));
    }

    private Mono<CreateConsentResponse> createSingleConsent(SingleConsentRequest request, String requestId) {
        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .post()
                .uri(SINGLE_CONSENTS_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), correlationId);
                    httpHeaders.add(INTERACTION_ID.getValue(), correlationId);
                })
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.getResponseMono(CreateConsentResponse.class));
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
