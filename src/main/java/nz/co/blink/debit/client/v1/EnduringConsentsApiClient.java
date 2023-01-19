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
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.OneOfauthFlowDetail;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.enums.BlinkDebitConstant;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import nz.co.blink.debit.helpers.ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static nz.co.blink.debit.enums.BlinkDebitConstant.ENDURING_CONSENTS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.INTERACTION_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The client for enduring consents.
 */
@Component
@Slf4j
public class EnduringConsentsApiClient {

    private final ReactorClientHttpConnector connector;

    private final String debitUrl;

    private final AccessTokenHandler accessTokenHandler;

    private final Validator validator;

    private final Retry retry;

    private WebClient.Builder webClientBuilder;

    /**
     * Default constructor.
     *
     * @param connector          the {@link ReactorClientHttpConnector}
     * @param properties         the {@link BlinkPayProperties}
     * @param accessTokenHandler the {@link AccessTokenHandler}
     * @param validator          the {@link Validator}
     * @param retry              the {@link Retry} instance
     */
    @Autowired
    public EnduringConsentsApiClient(@Qualifier("blinkDebitClientHttpConnector") ReactorClientHttpConnector connector,
                                     BlinkPayProperties properties, AccessTokenHandler accessTokenHandler,
                                     Validator validator, Retry retry) {
        this.connector = connector;
        debitUrl = properties.getDebit().getUrl();
        this.accessTokenHandler = accessTokenHandler;
        this.validator = validator;
        this.retry = retry;
    }

    /**
     * Creates an enduring consent.
     *
     * @param request the {@link EnduringConsentRequest}
     * @return the {@link CreateConsentResponse} {@link Mono}
     * @throws BlinkInvalidValueException thrown when one or more arguments are invalid
     */
    public Mono<CreateConsentResponse> createEnduringConsent(EnduringConsentRequest request)
            throws BlinkInvalidValueException {
        return createEnduringConsent(request, null);
    }

    /**
     * Creates an enduring consent.
     *
     * @param request   the {@link EnduringConsentRequest}
     * @param requestId the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     * @throws BlinkInvalidValueException thrown when one or more arguments are invalid
     */
    public Mono<CreateConsentResponse> createEnduringConsent(EnduringConsentRequest request, final String requestId)
            throws BlinkInvalidValueException {
        if (request == null) {
            throw new BlinkInvalidValueException("Enduring consent request must not be null");
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

        if (request.getPeriod() == null) {
            throw new BlinkInvalidValueException("Period must not be null");
        }

        if (request.getFromTimestamp() == null) {
            throw new BlinkInvalidValueException("Start date must not be null");
        }

        Amount amount = request.getMaximumAmountPeriod();
        if (amount == null) {
            throw new BlinkInvalidValueException("Maximum amount period must not be null");
        }

        if (amount.getCurrency() == null) {
            throw new BlinkInvalidValueException("Currency must not be null");
        }

        Set<ConstraintViolation<EnduringConsentRequest>> violations = new HashSet<>(validator.validate(request));
        if (!violations.isEmpty()) {
            String constraintViolations = violations.stream()
                    .map(cv -> cv == null ? "null" : cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.joining(", "));
            log.error("Validation failed for single consent request: {}", constraintViolations);
            throw new BlinkInvalidValueException(String.format("Validation failed for enduring consent request: %s",
                    violations));
        }

        return createEnduringConsentMono(request, requestId);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @return the {@link Consent} {@link Mono}
     * @throws BlinkInvalidValueException thrown when one or more arguments are invalid
     */
    public Mono<Consent> getEnduringConsent(UUID consentId) throws BlinkInvalidValueException {
        return getEnduringConsent(consentId, null);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     * @return the {@link Consent} {@link Mono}
     * @throws BlinkInvalidValueException thrown when one or more arguments are invalid
     */
    public Mono<Consent> getEnduringConsent(UUID consentId, final String requestId)
            throws BlinkInvalidValueException {
        if (consentId == null) {
            throw new BlinkInvalidValueException("Consent ID must not be null");
        }

        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDURING_CONSENTS_PATH.getValue() + "/{consentId}")
                        .build(consentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), correlationId);
                    httpHeaders.add(INTERACTION_ID.getValue(), correlationId);
                })
                .exchangeToMono(ResponseHandler.handleResponseMono(Consent.class));
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @throws BlinkInvalidValueException thrown when one or more arguments are invalid
     */
    public Mono<Void> revokeEnduringConsent(UUID consentId) throws BlinkInvalidValueException {
        return revokeEnduringConsent(consentId, null);
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     * @throws BlinkInvalidValueException thrown when one or more arguments are invalid
     */
    public Mono<Void> revokeEnduringConsent(UUID consentId, final String requestId)
            throws BlinkInvalidValueException {
        if (consentId == null) {
            throw new BlinkInvalidValueException("Consent ID must not be null");
        }

        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDURING_CONSENTS_PATH.getValue() + "/{consentId}")
                        .build(consentId))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), correlationId);
                    httpHeaders.add(INTERACTION_ID.getValue(), correlationId);
                })
                .exchangeToMono(ResponseHandler.handleResponseMono(Void.class))
                .transformDeferred(RetryOperator.of(retry));
    }

    private Mono<CreateConsentResponse> createEnduringConsentMono(EnduringConsentRequest request, String requestId)
            throws BlinkInvalidValueException {
        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder(correlationId)
                .build()
                .post()
                .uri(ENDURING_CONSENTS_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), correlationId);
                    httpHeaders.add(INTERACTION_ID.getValue(), correlationId);
                })
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.handleResponseMono(CreateConsentResponse.class))
                .transformDeferred(RetryOperator.of(retry));
    }

    private WebClient.Builder getWebClientBuilder(String requestId) throws BlinkInvalidValueException {
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
