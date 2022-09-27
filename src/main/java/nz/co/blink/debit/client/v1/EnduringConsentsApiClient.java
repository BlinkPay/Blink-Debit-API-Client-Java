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
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.OneOfauthFlowDetail;
import nz.co.blink.debit.dto.v1.Period;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.RedirectFlowHint;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import nz.co.blink.debit.helpers.ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static nz.co.blink.debit.enums.BlinkDebitConstant.ENDURING_CONSENTS_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.INTERACTION_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The client for enduring consents.
 */
@Component
public class EnduringConsentsApiClient {

    private final WebClient.Builder webClientBuilder;

    private final AccessTokenHandler accessTokenHandler;

    /**
     * Default constructor.
     *
     * @param webClientBuilder   the {@link WebClient.Builder}
     * @param accessTokenHandler the {@link AccessTokenHandler}
     */
    @Autowired
    public EnduringConsentsApiClient(@Qualifier("blinkDebitWebClientBuilder") WebClient.Builder webClientBuilder,
                                     AccessTokenHandler accessTokenHandler) {
        this.webClientBuilder = webClientBuilder;
        this.accessTokenHandler = accessTokenHandler;
    }

    /**
     * Creates an enduring payment consent request with redirect flow.
     *
     * @param type            the {@link AuthFlowDetail.TypeEnum}
     * @param bank            the {@link Bank}
     * @param redirectUri     the redirect URI
     * @param period          the {@link Period}
     * @param fromTimestamp   the ISO 8601 start date to calculate the periods for which to calculate the consent period.
     * @param expiryTimestamp the ISO 8601 timeout for when an enduring consent will expire
     * @param total           the total
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsent(AuthFlowDetail.TypeEnum type, Bank bank,
                                                             final String redirectUri, Period period,
                                                             OffsetDateTime fromTimestamp,
                                                             OffsetDateTime expiryTimestamp, final String total) {
        return createEnduringConsent(type, bank, redirectUri, period, fromTimestamp, expiryTimestamp, total, null);
    }

    /**
     * Creates an enduring payment consent request with redirect flow.
     *
     * @param type            the {@link AuthFlowDetail.TypeEnum}
     * @param bank            the {@link Bank}
     * @param redirectUri     the redirect URI
     * @param period          the {@link Period}
     * @param fromTimestamp   the ISO 8601 start date to calculate the periods for which to calculate the consent period.
     * @param expiryTimestamp the ISO 8601 timeout for when an enduring consent will expire
     * @param total           the total
     * @param requestId       the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsent(AuthFlowDetail.TypeEnum type, Bank bank,
                                                             final String redirectUri, Period period,
                                                             OffsetDateTime fromTimestamp,
                                                             OffsetDateTime expiryTimestamp, final String total,
                                                             final String requestId) {
        return createEnduringConsent(type, bank, redirectUri, period, fromTimestamp, expiryTimestamp, total, null, null,
                null, null, requestId);
    }

    /**
     * Creates an enduring payment consent request with the bank that will go to the customer for approval.
     * A successful response does not indicate a completed consent.
     * The status of the consent can be subsequently checked with the consent ID.
     *
     * @param type            the {@link AuthFlowDetail.TypeEnum}
     * @param bank            the {@link Bank}
     * @param redirectUri     the redirect URI
     * @param period          the {@link Period}
     * @param fromTimestamp   the ISO 8601 start date to calculate the periods for which to calculate the consent period.
     * @param expiryTimestamp the ISO 8601 timeout for when an enduring consent will expire
     * @param total           the total
     * @param flowHintType    the {@link FlowHint.TypeEnum} for gateway flow
     * @param identifierType  the {@link IdentifierType} for decoupled flow
     * @param identifierValue the identifier value for decoupled flow
     * @param callbackUrl     the merchant callback/webhook URL for decoupled flow
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsent(AuthFlowDetail.TypeEnum type, Bank bank,
                                                             final String redirectUri, Period period,
                                                             OffsetDateTime fromTimestamp,
                                                             OffsetDateTime expiryTimestamp, final String total,
                                                             FlowHint.TypeEnum flowHintType,
                                                             IdentifierType identifierType,
                                                             final String identifierValue, final String callbackUrl) {
        return createEnduringConsent(type, bank, redirectUri, period, fromTimestamp, expiryTimestamp, total,
                flowHintType, identifierType, identifierValue, callbackUrl, null);
    }

    /**
     * Creates an enduring payment consent request with the bank that will go to the customer for approval.
     * A successful response does not indicate a completed consent.
     * The status of the consent can be subsequently checked with the consent ID.
     *
     * @param type            the {@link AuthFlowDetail.TypeEnum}
     * @param bank            the {@link Bank}
     * @param redirectUri     the redirect URI
     * @param period          the {@link Period}
     * @param fromTimestamp   the ISO 8601 start date to calculate the periods for which to calculate the consent period.
     * @param expiryTimestamp the ISO 8601 timeout for when an enduring consent will expire
     * @param total           the total
     * @param flowHintType    the {@link FlowHint.TypeEnum} for gateway flow
     * @param identifierType  the {@link IdentifierType} for decoupled flow
     * @param identifierValue the identifier value for decoupled flow
     * @param callbackUrl     the merchant callback/webhook URL for decoupled flow
     * @param requestId       the optional correlation ID
     * @return the {@link CreateConsentResponse} {@link Mono}
     */
    public Mono<CreateConsentResponse> createEnduringConsent(AuthFlowDetail.TypeEnum type, Bank bank,
                                                             final String redirectUri, Period period,
                                                             OffsetDateTime fromTimestamp,
                                                             OffsetDateTime expiryTimestamp, final String total,
                                                             FlowHint.TypeEnum flowHintType,
                                                             IdentifierType identifierType,
                                                             final String identifierValue, final String callbackUrl,
                                                             final String requestId) {
        if (AuthFlowDetail.TypeEnum.GATEWAY == type && flowHintType == null) {
            throw new IllegalArgumentException("Gateway flow type requires redirect or decoupled flow hint type");
        }

        if (bank == null) {
            throw new IllegalArgumentException("Bank must not be null");
        }

        FlowHint flowHint = null;
        if (flowHintType != null) {
            if (FlowHint.TypeEnum.REDIRECT == flowHintType) {
                flowHint = new RedirectFlowHint()
                        .bank(bank)
                        .type(flowHintType);
            } else if (FlowHint.TypeEnum.DECOUPLED == flowHintType) {
                if (identifierType == null) {
                    throw new IllegalArgumentException("Identifier type must not be null");
                }
                if (StringUtils.isBlank(identifierValue)) {
                    throw new IllegalArgumentException("Identifier value must not be blank");
                }
                flowHint = new DecoupledFlowHint()
                        .identifierType(identifierType)
                        .identifierValue(identifierValue)
                        .bank(bank)
                        .type(flowHintType);
            }
        }

        if (type == null) {
            throw new IllegalArgumentException("Authorisation flow must not be null");
        }
        OneOfauthFlowDetail detail = null;
        switch (type) {
            case REDIRECT:
                if (StringUtils.isBlank(redirectUri)) {
                    throw new IllegalArgumentException("Redirect URI must not be blank");
                }
                detail = (OneOfauthFlowDetail) new RedirectFlow()
                        .bank(bank)
                        .redirectUri(redirectUri)
                        .type(type);
                break;
            case DECOUPLED:
                if (identifierType == null) {
                    throw new IllegalArgumentException("Identifier type must not be null");
                }
                if (StringUtils.isBlank(identifierValue)) {
                    throw new IllegalArgumentException("Identifier value must not be blank");
                }
                if (StringUtils.isBlank(callbackUrl)) {
                    throw new IllegalArgumentException("Callback/webhook URL must not be blank");
                }
                detail = (OneOfauthFlowDetail) new DecoupledFlow()
                        .bank(bank)
                        .callbackUrl(callbackUrl)
                        .identifierType(identifierType)
                        .identifierValue(identifierValue)
                        .type(type);
                break;
            case GATEWAY:
                if (StringUtils.isBlank(redirectUri)) {
                    throw new IllegalArgumentException("Redirect URI must not be blank");
                }
                detail = (OneOfauthFlowDetail) new GatewayFlow()
                        .flowHint(flowHint)
                        .redirectUri(redirectUri)
                        .type(type);
                break;
        }

        if (period == null) {
            throw new IllegalArgumentException("Period must not be null");
        }

        if (fromTimestamp == null) {
            throw new IllegalArgumentException("Start date must not be null");
        }

        if (!NumberUtils.isParsable(total)) {
            throw new IllegalArgumentException("Total is not a valid amount");
        }

        EnduringConsentRequest request = (EnduringConsentRequest) new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(detail))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total(total))
                .period(period)
                .fromTimestamp(fromTimestamp)
                .expiryTimestamp(expiryTimestamp)
                .type(ConsentDetail.TypeEnum.ENDURING);

        String correlationId = StringUtils.isNotBlank(requestId) ? requestId : UUID.randomUUID().toString();

        return webClientBuilder
                .filter(accessTokenHandler.setAccessToken(correlationId))
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
                .exchangeToMono(ResponseHandler.getResponseMono(CreateConsentResponse.class));
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @return the {@link Consent} {@link Mono}
     */
    public Mono<Consent> getEnduringConsent(UUID consentId) {
        return getEnduringConsent(consentId, null);
    }

    /**
     * Retrieves an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     * @return the {@link Consent} {@link Mono}
     */
    public Mono<Consent> getEnduringConsent(UUID consentId, final String requestId) {
        if (consentId == null) {
            throw new IllegalArgumentException("Consent ID must not be null");
        }

        String correlationId = StringUtils.isNotBlank(requestId) ? requestId : UUID.randomUUID().toString();

        return webClientBuilder
                .filter(accessTokenHandler.setAccessToken(correlationId))
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
                .exchangeToMono(ResponseHandler.getResponseMono(Consent.class));
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     */
    public Mono<Void> revokeEnduringConsent(UUID consentId) {
        return revokeEnduringConsent(consentId, null);
    }

    /**
     * Revokes an existing consent by ID.
     *
     * @param consentId the consent ID
     * @param requestId the optional correlation ID
     */
    public Mono<Void> revokeEnduringConsent(UUID consentId, final String requestId) {
        if (consentId == null) {
            throw new IllegalArgumentException("Consent ID must not be null");
        }

        String correlationId = StringUtils.isNotBlank(requestId) ? requestId : UUID.randomUUID().toString();

        return webClientBuilder
                .filter(accessTokenHandler.setAccessToken(correlationId))
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
                .exchangeToMono(ResponseHandler.getResponseMono(Void.class));
    }
}
