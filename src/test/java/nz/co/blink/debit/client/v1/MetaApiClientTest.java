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

import nz.co.blink.debit.exception.ExpiredAccessTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * The test case for {@link MetaApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class MetaApiClientTest {

    @InjectMocks
    private MetaApiClient client;

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void getMetaWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.getMeta(requestId, "accessToken").blockFirst(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void getMetaWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                client.getMeta(UUID.randomUUID().toString(), accessToken).blockFirst(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that blank access token is handled")
    void getMetaWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                client.getMeta(UUID.randomUUID().toString(), accessToken).blockFirst(), ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }
}
