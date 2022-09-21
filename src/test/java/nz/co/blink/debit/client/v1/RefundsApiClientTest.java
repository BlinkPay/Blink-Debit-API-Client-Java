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

import nz.co.blink.debit.dto.v1.RefundDetail;
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
 * The test case for {@link RefundsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class RefundsApiClientTest {

    @InjectMocks
    private RefundsApiClient client;

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void createAccountNumberRefundWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createRefund(requestId,
                        "accessToken", RefundDetail.TypeEnum.ACCOUNT_NUMBER, UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null payment ID is handled")
    void createAccountNumberRefundWithNullPaymentId() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createRefund(UUID.randomUUID().toString(),
                        "accessToken", RefundDetail.TypeEnum.ACCOUNT_NUMBER, null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Payment ID must not be null");
    }

    @Test
    @DisplayName("Verify that null refund type is handled")
    void createAccountNumberRefundWithNullRefundType() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createRefund(UUID.randomUUID().toString(),
                "accessToken", null, UUID.randomUUID()).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Refund type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that invalid particulars is handled")
    void createFullRefundWithInvalidParticulars(String particulars) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createRefund(UUID.randomUUID().toString(),
                "accessToken", RefundDetail.TypeEnum.FULL_REFUND, UUID.randomUUID(), "redirectUri", particulars,
                "code", "reference", "25.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank redirect URI is handled")
    void createFullRefundWithBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createRefund(UUID.randomUUID().toString(),
                "accessToken", RefundDetail.TypeEnum.FULL_REFUND, UUID.randomUUID(), redirectUri, "particulars",
                "code", "reference", "25.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that invalid particulars is handled")
    void createPartialRefundWithInvalidParticulars(String particulars) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createRefund(UUID.randomUUID().toString(),
                "accessToken", RefundDetail.TypeEnum.PARTIAL_REFUND, UUID.randomUUID(), "redirectUri", particulars,
                "code", "reference", "25.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank redirect URI is handled")
    void createPartialRefundWithBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createRefund(UUID.randomUUID().toString(),
                "accessToken", RefundDetail.TypeEnum.PARTIAL_REFUND, UUID.randomUUID(), redirectUri, "particulars",
                "code", "reference", "25.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createPartialRefundWithInvalidTotal(String total) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createRefund(UUID.randomUUID().toString(),
                "accessToken", RefundDetail.TypeEnum.PARTIAL_REFUND, UUID.randomUUID(), "redirectUri", "particulars",
                "code", "reference", total).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Total is not a valid amount");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void getRefundWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getRefund(requestId, "accessToken", UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null refund ID is handled")
    void getRefundWithNullRefundId() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getRefund(UUID.randomUUID().toString(), "accessToken", null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Refund ID must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void getRefundWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getRefund(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that expired access token is handled")
    void getRefundWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                        client.getRefund(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }
}
