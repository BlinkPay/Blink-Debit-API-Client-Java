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
import nz.co.blink.debit.dto.v1.BankMetadata;
import nz.co.blink.debit.exception.ExpiredAccessTokenException;
import nz.co.blink.debit.helpers.ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Date;

import static nz.co.blink.debit.enums.BlinkDebitConstant.METADATA_PATH;
import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The client for bank metadata.
 */
@Component
public class MetaApiClient {

    private final WebClient webClient;

    /**
     * Default constructor.
     *
     * @param webClient the {@link WebClient}
     */
    @Autowired
    public MetaApiClient(@Qualifier("blinkDebitWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Returns the {@link BankMetadata} {@link Flux}.
     *
     * @param requestId   the correlation ID
     * @param accessToken the OAuth2 access token
     * @return the {@link BankMetadata} {@link Flux}
     * @throws ExpiredAccessTokenException thrown when the access token has expired after 1 day
     */
    public Flux<BankMetadata> getMeta(final String requestId, final String accessToken)
            throws ExpiredAccessTokenException {
        if (StringUtils.isBlank(requestId)) {
            throw new IllegalArgumentException("Request ID must not be blank");
        }

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("Access token must not be blank");
        }
        DecodedJWT jwt = JWT.decode(accessToken);
        if (jwt.getExpiresAt().before(new Date())) {
            throw new ExpiredAccessTokenException();
        }

        String authorization = "Bearer " + accessToken;

        return webClient
                .get()
                .uri(METADATA_PATH.getValue())
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.add(REQUEST_ID.getValue(), requestId);
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);
                })
                .exchangeToFlux(ResponseHandler.getResponseFlux(BankMetadata.class));
    }
}
