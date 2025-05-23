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
import nz.co.blink.debit.enums.BlinkDebitConstant;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import nz.co.blink.debit.helpers.RequestHandler;
import nz.co.blink.debit.helpers.ResponseHandler;
import nz.co.blink.debit.service.ValidationService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static nz.co.blink.debit.enums.BlinkDebitConstant.CUSTOMER_IP;
import static nz.co.blink.debit.enums.BlinkDebitConstant.CUSTOMER_USER_AGENT;
import static nz.co.blink.debit.enums.BlinkDebitConstant.IDEMPOTENCY_KEY;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.SINGLE_CONSENTS_PATH;

/**
 * The client for single consents.
 */
@Component
@Slf4j
public class SingleConsentsApiClient {

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
    public SingleConsentsApiClient(@Qualifier("blinkDebitClientHttpConnector") ReactorClientHttpConnector connector,
                                   BlinkPayProperties properties, AccessTokenHandler accessTokenHandler,
                                   ValidationService validationService, Retry retry) {
        this.connector = connector;
        debitUrl = properties.getDebit().getUrl();
        this.accessTokenHandler = accessTokenHandler;
        this.validationService = validationService;
        this.retry = retry;
    }

    /**
     * Creates a single consent.
     *
     * @param request the {@link SingleConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<CreateConsentResponse> createSingleConsent(SingleConsentRequest request)
            throws BlinkServiceException {
        return createSingleConsent(request, new HashMap<>());
    }

    /**
     * Creates a single consent.
     *
     * @param request   the {@link SingleConsentRequest}
     * @param requestId the optional request ID. If provided, it overrides the interaction ID generated by Blink Debit.
     * @return the {@link CreateConsentResponse} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<CreateConsentResponse> createSingleConsent(SingleConsentRequest request, final String requestId)
            throws BlinkServiceException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(REQUEST_ID.getValue(), requestId);

        return createSingleConsent(request, requestHeaders);
    }

    /**
     * Creates a single consent.
     *
     * @param request        the {@link SingleConsentRequest}
     * @param requestHeaders the {@link Map} of optional request headers
     * @return the {@link CreateConsentResponse} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<CreateConsentResponse> createSingleConsent(SingleConsentRequest request,
                                                           Map<String, String> requestHeaders)
            throws BlinkServiceException {
        if (request == null) {
            throw new BlinkInvalidValueException("Single consent request must not be null");
        }

        AuthFlow flow = request.getFlow();
        if (flow == null) {
            throw new BlinkInvalidValueException("Authorisation flow must not be null");
        }

        OneOfauthFlowDetail detail = flow.getDetail();
        if (detail == null) {
            throw new BlinkInvalidValueException("Authorisation flow detail must not be null");
        }

        if (detail instanceof RedirectFlow) {
            RedirectFlow redirectFlow = (RedirectFlow) flow.getDetail();
            if (redirectFlow.getBank() == null) {
                throw new BlinkInvalidValueException("Bank must not be null");
            }

            if (StringUtils.isBlank(redirectFlow.getRedirectUri())) {
                throw new BlinkInvalidValueException("Redirect URI must not be blank");
            }
        } else if (detail instanceof DecoupledFlow) {
            DecoupledFlow decoupledFlow = (DecoupledFlow) flow.getDetail();
            if (decoupledFlow.getBank() == null) {
                throw new BlinkInvalidValueException("Bank must not be null");
            }

            if (decoupledFlow.getIdentifierType() == null) {
                throw new BlinkInvalidValueException("Identifier type must not be null");
            }

            if (StringUtils.isBlank(decoupledFlow.getIdentifierValue())) {
                throw new BlinkInvalidValueException("Identifier value must not be blank");
            }

            if (StringUtils.isBlank(decoupledFlow.getCallbackUrl())) {
                throw new BlinkInvalidValueException("Callback/webhook URL must not be blank");
            }
        } else if (detail instanceof GatewayFlow) {
            GatewayFlow gatewayFlow = (GatewayFlow) flow.getDetail();
            if (StringUtils.isBlank(gatewayFlow.getRedirectUri())) {
                throw new BlinkInvalidValueException("Redirect URI must not be blank");
            }

            FlowHint flowHint = gatewayFlow.getFlowHint();
            if (flowHint != null) {
                if (flowHint.getBank() == null) {
                    throw new BlinkInvalidValueException("Bank must not be null");
                }

                FlowHint.TypeEnum flowHintType = flowHint.getType();
                if (flowHintType == null) {
                    throw new BlinkInvalidValueException("Flow hint type must not be null");
                }

                if (FlowHint.TypeEnum.DECOUPLED == flowHintType) {
                    DecoupledFlowHint decoupledFlowHint = (DecoupledFlowHint) flowHint;
                    if (decoupledFlowHint.getIdentifierType() == null) {
                        throw new BlinkInvalidValueException("Identifier type must not be null");
                    }

                    if (StringUtils.isBlank(decoupledFlowHint.getIdentifierValue())) {
                        throw new BlinkInvalidValueException("Identifier value must not be blank");
                    }
                }
            }
        }

        Pcr pcr = request.getPcr();
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

        Amount amount = request.getAmount();
        if (amount == null) {
            throw new BlinkInvalidValueException("Amount must not be null");
        }

        if (amount.getCurrency() == null) {
            throw new BlinkInvalidValueException("Currency must not be null");
        }

        validationService.validateRequest("single consent", request);

        return createSingleConsentMono(request, requestHeaders);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @return the {@link Consent} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<Consent> getSingleConsent(UUID consentId) throws BlinkServiceException {
        return getSingleConsent(consentId, new HashMap<>());
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional request ID. If provided, it overrides the interaction ID generated by Blink Debit.
     * @return the {@link Consent} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<Consent> getSingleConsent(UUID consentId, String requestId) throws BlinkServiceException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(REQUEST_ID.getValue(), requestId);

        return getSingleConsent(consentId, requestHeaders);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId      the consent ID
     * @param requestHeaders the {@link Map} of optional request headers
     * @return the {@link Consent} {@link Mono}
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<Consent> getSingleConsent(UUID consentId, Map<String, String> requestHeaders)
            throws BlinkServiceException {
        if (consentId == null) {
            throw new BlinkInvalidValueException("Consent ID must not be null");
        }

        String requestId = MapUtils.getString(requestHeaders, REQUEST_ID.getValue(), UUID.randomUUID().toString());
        String customerIp = MapUtils.getString(requestHeaders, CUSTOMER_IP.getValue(), (String) null);
        String customerUserAgent = MapUtils.getString(requestHeaders, CUSTOMER_USER_AGENT.getValue(), (String) null);

        return getWebClientBuilder(requestId)
                .filter((clientRequest, exchangeFunction) -> RequestHandler.logRequest(null, clientRequest,
                        exchangeFunction))
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(SINGLE_CONSENTS_PATH.getValue() + "/{consentId}")
                        .build(consentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(CUSTOMER_IP.getValue(), customerIp);
                    httpHeaders.add(CUSTOMER_USER_AGENT.getValue(), customerUserAgent);
                })
                .exchangeToMono(ResponseHandler.handleResponseMono(Consent.class));
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<Void> revokeSingleConsent(UUID consentId) throws BlinkServiceException {
        return revokeSingleConsent(consentId, new HashMap<>());
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional request ID. If provided, it overrides the interaction ID generated by Blink Debit.
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<Void> revokeSingleConsent(UUID consentId, final String requestId) throws BlinkServiceException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(REQUEST_ID.getValue(), requestId);

        return revokeSingleConsent(consentId, requestHeaders);
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId      the consent ID
     * @param requestHeaders the {@link Map} of optional request headers
     * @throws BlinkServiceException thrown when an exception occurs
     */
    public Mono<Void> revokeSingleConsent(UUID consentId, Map<String, String> requestHeaders)
            throws BlinkServiceException {
        if (consentId == null) {
            throw new BlinkInvalidValueException("Consent ID must not be null");
        }

        String requestId = MapUtils.getString(requestHeaders, REQUEST_ID.getValue(), UUID.randomUUID().toString());
        String customerIp = MapUtils.getString(requestHeaders, CUSTOMER_IP.getValue(), (String) null);
        String customerUserAgent = MapUtils.getString(requestHeaders, CUSTOMER_USER_AGENT.getValue(), (String) null);

        return getWebClientBuilder(requestId)
                .filter((clientRequest, exchangeFunction) -> RequestHandler.logRequest(null, clientRequest,
                        exchangeFunction))
                .build()
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(SINGLE_CONSENTS_PATH.getValue() + "/{consentId}")
                        .build(consentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(CUSTOMER_IP.getValue(), customerIp);
                    httpHeaders.add(CUSTOMER_USER_AGENT.getValue(), customerUserAgent);
                })
                .exchangeToMono(ResponseHandler.handleResponseMono(Void.class))
                .transformDeferred(RetryOperator.of(retry));
    }

    private Mono<CreateConsentResponse> createSingleConsentMono(SingleConsentRequest request,
                                                                Map<String, String> requestHeaders)
            throws BlinkServiceException {
        String requestId = MapUtils.getString(requestHeaders, REQUEST_ID.getValue(), UUID.randomUUID().toString());
        String customerIp = MapUtils.getString(requestHeaders, CUSTOMER_IP.getValue(), (String) null);
        String customerUserAgent = MapUtils.getString(requestHeaders, CUSTOMER_USER_AGENT.getValue(), (String) null);
        String idempotencyKey = UUID.randomUUID().toString();

        return getWebClientBuilder(requestId)
                .filter((clientRequest, exchangeFunction) -> RequestHandler.logRequest(request, clientRequest,
                        exchangeFunction))
                .build()
                .post()
                .uri(SINGLE_CONSENTS_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(IDEMPOTENCY_KEY.getValue(), idempotencyKey);
                    httpHeaders.add(CUSTOMER_IP.getValue(), customerIp);
                    httpHeaders.add(CUSTOMER_USER_AGENT.getValue(), customerUserAgent);
                })
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.handleResponseMono(CreateConsentResponse.class))
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
