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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * The test case for {@link PaymentsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PaymentsApiClientTest {

    @InjectMocks
    private PaymentsApiClient client;

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void createPaymentWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createPayment(requestId,
                "accessToken", UUID.randomUUID()).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null consent ID is handled")
    void createPaymentWithNullConsentId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createPayment(UUID.randomUUID().toString(),
                "accessToken", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Consent ID must not be null");
    }

    @Test
    @DisplayName("Verify that null account reference ID is handled")
    void createPaymentWithNullAccountReferenceId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createPayment(UUID.randomUUID().toString(),
                "accessToken", UUID.randomUUID(), null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Account reference ID must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createEnduringPaymentWithInvalidTotal(String total) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createPayment(UUID.randomUUID().toString(),
                        "accessToken", UUID.randomUUID(), null, "particulars", "code", "reference", total).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Total is not a valid amount");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void getPaymentWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getPayment(requestId, "accessToken", UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null refund ID is handled")
    void getPaymentWithNullPaymentId() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getPayment(UUID.randomUUID().toString(), "accessToken", null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Payment ID must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void getPaymentWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getPayment(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that expired access token is handled")
    void getPaymentWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                        client.getPayment(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }
}
