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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import nz.co.blink.debit.config.BlinkDebitConfiguration;
import nz.co.blink.debit.dto.v1.AccessTokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static nz.co.blink.debit.enums.BlinkDebitConstant.TOKEN_PATH;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The component test case for {@link OAuthApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {OAuthApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
class OAuthApiClientComponentTest {

    @Autowired
    private OAuthApiClient client;

    @Test
    @DisplayName("Verify that access token is generated")
    void generateAccessToken() throws JsonProcessingException {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(System.getenv("ACCESS_TOKEN"));
        response.setTokenType("Bearer");
        response.setExpiresIn(86400);
        response.setScope("create:payment view:payment create:single_consent view:single_consent view:metadata create:enduring_consent view:enduring_consent revoke:enduring_consent view:transaction create:quick_payment view:quick_payment create:refund view:refund revoke:single_consent");

        stubFor(post(TOKEN_PATH.getValue())
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(JsonMapper.builder().build().writeValueAsString(response))));

        Mono<AccessTokenResponse> accessTokenResponseMono = client.generateAccessToken(UUID.randomUUID().toString());

        assertThat(accessTokenResponseMono).isNotNull();
        AccessTokenResponse actual = accessTokenResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(AccessTokenResponse::getTokenType, AccessTokenResponse::getExpiresIn,
                        AccessTokenResponse::getRefreshToken, AccessTokenResponse::getIdToken,
                        AccessTokenResponse::getScope)
                .containsExactly("Bearer", 86400, null, null, "create:payment view:payment create:single_consent"
                        + " view:single_consent view:metadata create:enduring_consent view:enduring_consent"
                        + " revoke:enduring_consent view:transaction create:quick_payment view:quick_payment"
                        + " create:refund view:refund revoke:single_consent");
        assertThat(actual.getAccessToken())
                .isNotBlank()
                .startsWith("ey");
    }
}
