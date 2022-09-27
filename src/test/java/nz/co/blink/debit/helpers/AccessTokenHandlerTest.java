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
package nz.co.blink.debit.helpers;

import nz.co.blink.debit.client.v1.OAuthApiClient;
import nz.co.blink.debit.dto.v1.AccessTokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link AccessTokenHandler}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AccessTokenHandlerTest {

    @Mock
    private OAuthApiClient client;

    @InjectMocks
    private AccessTokenHandler handler;

    @Test
    @DisplayName("Verify that new access token is generated")
    void setAccessTokenOnFirstRequest() {
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken("header.payload.signature");
        when(client.generateAccessToken(any(String.class)))
                .thenReturn(Mono.just(accessTokenResponse));

        WebClient webClient = WebClient.builder()
                .filter(handler.setAccessToken(UUID.randomUUID().toString()))
                .build();
        String body = webClient
                .get()
                .uri("https://eo7mwezdiddjlbo.m.pipedream.net/?accessTokenTest=" + UUID.randomUUID())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        assertThat(body).isEqualTo("Bearer " + accessTokenResponse.getAccessToken());
    }

    @Test
    @DisplayName("Verify that existing access token is reused")
    void setAccessTokenWhenAccessTokenExists() {
        handler.setAccessTokenAtomicReference(System.getenv("ACCESS_TOKEN"));

        WebClient webClient = WebClient.builder()
                .filter(handler.setAccessToken(UUID.randomUUID().toString()))
                .build();
        String body = webClient
                .get()
                .uri("https://eo7mwezdiddjlbo.m.pipedream.net/?accessTokenTest=" + UUID.randomUUID())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        assertThat(body).isEqualTo("Bearer " + System.getenv("ACCESS_TOKEN"));
    }
}
