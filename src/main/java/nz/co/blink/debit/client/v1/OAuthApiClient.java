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
import nz.co.blink.debit.config.BlinkPayProperties;
import nz.co.blink.debit.dto.v1.AccessTokenRequest;
import nz.co.blink.debit.dto.v1.AccessTokenResponse;
import nz.co.blink.debit.enums.BlinkDebitConstant;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
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

import java.util.UUID;

import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.TOKEN_PATH;

/**
 * The client for OAuth2.
 */
@Component
public class OAuthApiClient {

    private final ReactorClientHttpConnector connector;

    private final String debitUrl;

    private final String clientId;

    private final String clientSecret;

    private final Retry retry;

    private WebClient.Builder webClientBuilder;

    /**
     * Default constructor.
     *
     * @param connector  the {@link ReactorClientHttpConnector}
     * @param properties the {@link BlinkPayProperties}
     * @param retry      the {@link Retry} instance
     */
    @Autowired
    public OAuthApiClient(@Qualifier("blinkDebitClientHttpConnector") ReactorClientHttpConnector connector,
                          BlinkPayProperties properties, Retry retry) {
        this.connector = connector;
        this.retry = retry;
        debitUrl = properties.getDebit().getUrl();
        clientId = properties.getClient().getId();
        clientSecret = properties.getClient().getSecret();
    }

    /**
     * Generates an access token valid for 1 day.
     *
     * @return the {@link AccessTokenResponse} {@link Mono}
     * @throws BlinkInvalidValueException thrown when one or more arguments are invalid
     */
    public Mono<AccessTokenResponse> generateAccessToken() throws BlinkInvalidValueException {
        return generateAccessToken(null);
    }

    /**
     * Generates an access token valid for 1 day.
     *
     * @param requestId the optional correlation ID
     * @return the {@link AccessTokenResponse} {@link Mono}
     * @throws BlinkInvalidValueException thrown when one or more arguments are invalid
     */
    public Mono<AccessTokenResponse> generateAccessToken(final String requestId) throws BlinkInvalidValueException {
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            throw new BlinkInvalidValueException("Client ID and client secret must not be blank");
        }

        AccessTokenRequest request = AccessTokenRequest.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("client_credentials")
                .build();

        String correlationId = StringUtils.defaultIfBlank(requestId, UUID.randomUUID().toString());

        return getWebClientBuilder()
                .build()
                .post()
                .uri(TOKEN_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add(REQUEST_ID.getValue(), correlationId))
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.handleResponseMono(AccessTokenResponse.class))
                .transformDeferred(RetryOperator.of(retry));
    }

    private WebClient.Builder getWebClientBuilder() {
        if (webClientBuilder != null) {
            return webClientBuilder;
        }

        return WebClient.builder()
                .clientConnector(connector)
                .defaultHeader(HttpHeaders.USER_AGENT, BlinkDebitConstant.USER_AGENT_VALUE.getValue())
                .baseUrl(debitUrl);
    }
}
