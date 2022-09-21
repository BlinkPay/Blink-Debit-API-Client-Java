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

import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.FlowHint;
import nz.co.blink.debit.dto.v1.IdentifierType;
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
 * The test case for {@link QuickPaymentsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class QuickPaymentsApiClientTest {

    @InjectMocks
    private QuickPaymentsApiClient client;

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void createQuickPaymentWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(requestId,
                "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080",
                "particulars", "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null authorisation flow hint is handled")
    void createQuickPaymentWithGatewayFlowAndNullFlowHint() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, "http://localhost:8080",
                "particulars", "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Gateway flow type requires redirect or decoupled flow hint type");
    }

    @Test
    @DisplayName("Verify that null bank is handled")
    void createQuickPaymentWithNullBank() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, null, "http://localhost:8080",
                "particulars", "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Bank must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow is handled")
    void createQuickPaymentWithNullAuthorisationFlow() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                "accessToken", null, Bank.PNZ, "http://localhost:8080", "particulars", "code", "reference",
                "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that redirect flow with blank redirect URI is handled")
    void createQuickPaymentWithRedirectFlowAndBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, redirectUri, "particulars", "code", "reference",
                "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that decoupled flow with null identifier type is handled")
    void createQuickPaymentWithDecoupledFlowAndNullIdentifierType() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                        "accessToken", AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, "particulars", "code",
                        "reference", "1.25", null, null, "+6449144425", "callbackUrl").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank redirect URI is handled")
    void createQuickPaymentWithDecoupledFlowAndBlankIdentifierValue(String identifierValue) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                        "accessToken", AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, "particulars", "code",
                        "reference", "1.25", null, IdentifierType.PHONE_NUMBER, identifierValue, "callbackUrl").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank callback URL is handled")
    void createQuickPaymentWithDecoupledFlowAndBlankCallbackUrl(String callbackUrl) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                        "accessToken", AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, "particulars", "code",
                        "reference", "1.25", null, IdentifierType.PHONE_NUMBER, "+6449144425", callbackUrl).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Callback/webhook URL must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank redirect URI is handled")
    void createQuickPaymentWithGatewayFlowAndBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, redirectUri, "particulars", "code", "reference",
                "1.25", FlowHint.TypeEnum.REDIRECT, null, null, null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that gateway flow with null identifier type is handled")
    void createQuickPaymentWithGatewayFlowAndNullIdentifierType() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                        "accessToken", AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, null, "particulars", "code",
                        "reference", "1.25", FlowHint.TypeEnum.DECOUPLED, null, "+6449144425", "callbackUrl").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank redirect URI is handled")
    void createQuickPaymentWithGatewayFlowAndBlankIdentifierValue(String identifierValue) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, null, "particulars", "code",
                "reference", "1.25", FlowHint.TypeEnum.DECOUPLED, IdentifierType.PHONE_NUMBER, identifierValue,
                null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that invalid particulars is handled")
    void createQuickPaymentWithInvalidParticulars(String particulars) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080", particulars,
                "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Particulars must have at least 1 character");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createQuickPaymentWithInvalidTotal(String total) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080", "particulars",
                "code", "reference", total, null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Total is not a valid amount");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void createQuickPaymentWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createQuickPayment(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080",
                "particulars", "code", "reference", "1.25", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that expired access token is handled")
    void createQuickPaymentWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                client.createQuickPayment(UUID.randomUUID().toString(), accessToken,
                        AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080", "particulars", "code",
                        "reference", "1.25", null).block(), ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void getQuickPaymentWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getQuickPayment(requestId, "accessToken", UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null quick payment ID is handled")
    void getQuickPaymentWithNullQuickPaymentId() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getQuickPayment(UUID.randomUUID().toString(), "accessToken", null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Quick payment ID must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void getQuickPaymentWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getQuickPayment(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that expired access token is handled")
    void getQuickPaymentWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                        client.getQuickPayment(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void revokeQuickPaymentWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.revokeQuickPayment(requestId, "accessToken", UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null quick payment ID is handled")
    void revokeQuickPaymentWithNullQuickPaymentId() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.revokeQuickPayment(UUID.randomUUID().toString(), "accessToken", null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Quick payment ID must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void revokeQuickPaymentWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.revokeQuickPayment(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that expired access token is handled")
    void revokeQuickPaymentWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                        client.revokeQuickPayment(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }
}