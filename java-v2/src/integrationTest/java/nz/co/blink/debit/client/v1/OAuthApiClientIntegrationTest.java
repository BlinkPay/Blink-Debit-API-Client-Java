/**
 * Copyright (c) 2025 BlinkPay
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

import nz.co.blink.debit.config.BlinkDebitConfig;
import nz.co.blink.debit.dto.v1.AccessTokenResponse;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link OAuthApiClient}.
 * Tests OAuth2 client credentials flow for access token generation.
 */
@Tag("integration")
class OAuthApiClientIntegrationTest {

    private static BlinkDebitClient client;
    private static boolean credentialsAvailable;

    @BeforeAll
    static void setUp() throws BlinkInvalidValueException {
        String debitUrl = System.getenv("BLINKPAY_DEBIT_URL");
        String clientId = System.getenv("BLINKPAY_CLIENT_ID");
        String clientSecret = System.getenv("BLINKPAY_CLIENT_SECRET");

        credentialsAvailable = debitUrl != null && clientId != null && clientSecret != null;

        if (credentialsAvailable) {
            BlinkDebitConfig config = BlinkDebitConfig.builder()
                    .debitUrl(debitUrl)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            client = new BlinkDebitClient(config);
        }
    }

    @AfterAll
    static void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    @DisplayName("Verify that OAuth2 access token is generated with correct format and scopes")
    void generateAccessToken() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        AccessTokenResponse response = client.getOAuthApi().generateAccessToken(UUID.randomUUID().toString());

        assertThat(response)
                .isNotNull()
                .satisfies(token -> {
                    // Validate token type
                    assertThat(token.getTokenType())
                            .isNotBlank()
                            .isEqualTo("Bearer");

                    // Validate expiry (should be 3600 seconds = 1 hour)
                    assertThat(token.getExpiresIn())
                            .isNotNull()
                            .isEqualTo(3600);

                    // Validate access token format (JWT tokens start with "ey")
                    assertThat(token.getAccessToken())
                            .isNotBlank()
                            .startsWith("ey");

                    // Validate scopes are present
                    assertThat(token.getScope())
                            .isNotBlank()
                            .contains("create:payment")
                            .contains("view:payment")
                            .contains("view:metadata")
                            .contains("create:single_consent")
                            .contains("view:single_consent")
                            .contains("revoke:single_consent")  // covers quick payments too
                            .contains("create:enduring_consent")
                            .contains("view:enduring_consent")
                            .contains("revoke:enduring_consent")
                            .contains("create:quick_payment")
                            .contains("view:quick_payment")
                            // Note: revoke:quick_payment scope doesn't exist - covered by revoke:single_consent
                            .contains("create:refund")
                            .contains("view:refund");
                });
    }
}
