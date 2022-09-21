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

import nz.co.blink.debit.dto.v1.AccessTokenRequest;
import nz.co.blink.debit.dto.v1.AccessTokenResponse;
import nz.co.blink.debit.helpers.ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;
import static nz.co.blink.debit.enums.BlinkDebitConstant.TOKEN_PATH;

/**
 * The client for OAuth2.
 */
@Component
public class OAuthApiClient {

    private final WebClient webClient;

    @Value("${blinkpay.client.id:}")
    private String clientId;

    @Value("${blinkpay.client.secret:}")
    private String clientSecret;

    /**
     * Default constructor.
     * @param webClient the {@link WebClient}
     */
    @Autowired
    public OAuthApiClient(@Qualifier("blinkDebitWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Generates an access token valid for 1 day.
     *
     * @param requestId the correlation ID
     * @return the {@link AccessTokenResponse} {@link Mono}
     */
    public Mono<AccessTokenResponse> generateAccessToken(final String requestId) {
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            throw new IllegalArgumentException("Client ID and client secret must not be blank");
        }

        if (StringUtils.isBlank(requestId)) {
            throw new IllegalArgumentException("Request ID must not be blank");
        }

        AccessTokenRequest request = AccessTokenRequest.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("client_credentials")
                .build();

        return webClient
                .post()
                .uri(TOKEN_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.add(REQUEST_ID.getValue(), requestId))
                .bodyValue(request)
                .exchangeToMono(ResponseHandler.getResponseMono(AccessTokenResponse.class));
    }
}
