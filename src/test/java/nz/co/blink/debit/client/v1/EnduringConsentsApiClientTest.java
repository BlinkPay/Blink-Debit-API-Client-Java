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
import nz.co.blink.debit.dto.v1.Period;
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

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * The test case for {@link EnduringConsentsApiClient}.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class EnduringConsentsApiClientTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

    @InjectMocks
    private EnduringConsentsApiClient client;

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void createEnduringConsentWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(requestId,
                        "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "http://localhost:8080",
                        Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null authorisation flow hint is handled")
    void createEnduringConsentWithGatewayFlowAndNullFlowHint() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, "http://localhost:8080",
                Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER,
                "+6449144425", null).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Gateway flow type requires redirect or decoupled flow hint type");
    }

    @Test
    @DisplayName("Verify that null bank is handled")
    void createEnduringConsentWithNullBank() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                        "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, null, "http://localhost:8080",
                        Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Bank must not be null");
    }

    @Test
    @DisplayName("Verify that null authorisation flow is handled")
    void createEnduringConsentWithNullAuthorisationFlow() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", null, Bank.PNZ, "http://localhost:8080", Period.MONTHLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Authorisation flow must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that redirect flow with blank redirect URI is handled")
    void createEnduringConsentWithRedirectFlowAndBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                        "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, redirectUri,
                        Period.MONTHLY, OffsetDateTime.now(ZONE_ID), null, "50.00").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that decoupled flow with null identifier type is handled")
    void createEnduringConsentWithDecoupledFlowAndNullIdentifierType() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                        "accessToken", AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.MONTHLY,
                        OffsetDateTime.now(ZONE_ID), null, "50.00", null, null, "+6449144425", "callbackUrl").block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank identifier value is handled")
    void createEnduringConsentWithDecoupledFlowAndBlankIdentifierValue(String identifierValue) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.MONTHLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER, identifierValue,
                "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that decoupled flow with blank callback URL is handled")
    void createEnduringConsentWithDecoupledFlowAndBlankCallbackUrl(String callbackUrl) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.MONTHLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425",
                callbackUrl).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Callback/webhook URL must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank redirect URI is handled")
    void createEnduringConsentWithGatewayFlowAndBlankRedirectUri(String redirectUri) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, redirectUri, Period.MONTHLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.REDIRECT, IdentifierType.PHONE_NUMBER, "+6449144425",
                "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Redirect URI must not be blank");
    }

    @Test
    @DisplayName("Verify that gateway flow with null identifier type is handled")
    void createEnduringConsentWithGatewayFlowAndNullIdentifierType() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, null, Period.MONTHLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.DECOUPLED, null, "+6449144425",
                "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier type must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that gateway flow with blank identifier value is handled")
    void createEnduringConsentWithGatewayFlowAndBlankIdentifierValue(String identifierValue) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.GATEWAY, Bank.PNZ, null, Period.MONTHLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00", FlowHint.TypeEnum.DECOUPLED, IdentifierType.PHONE_NUMBER,
                identifierValue, "callbackUrl").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Identifier value must not be blank");
    }

    @Test
    @DisplayName("Verify that null period is handled")
    void createEnduringConsentWithNullPeriod() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "redirectUri", null,
                OffsetDateTime.now(ZONE_ID), null, "50.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Period must not be null");
    }

    @Test
    @DisplayName("Verify that null start date is handled")
    void createEnduringConsentWithNullStartDate() {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "redirectUri", Period.MONTHLY,
                null, null, "50.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Start date must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc.de", "/!@#$%^&*()[{}]/=',.\"<>`~;:|\\"})
    @DisplayName("Verify that invalid total is handled")
    void createEnduringConsentWithInvalidTotal(String total) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                "accessToken", AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "redirectUri", Period.MONTHLY,
                OffsetDateTime.now(ZONE_ID), null, total).block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Total is not a valid amount");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void createEnduringConsentWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() -> client.createEnduringConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "redirectUri", Period.MONTHLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00").block(), IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that expired access token is handled")
    void createEnduringConsentWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                        client.createEnduringConsent(UUID.randomUUID().toString(), accessToken,
                                AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, "redirectUri", Period.MONTHLY,
                                OffsetDateTime.now(ZONE_ID), null, "50.00").block(),
                ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void getEnduringConsentWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getEnduringConsent(requestId, "accessToken", UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null consent ID is handled")
    void getEnduringConsentWithNullConsentId() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getEnduringConsent(UUID.randomUUID().toString(), "accessToken", null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Consent ID must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void getEnduringConsentWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.getEnduringConsent(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that expired access token is handled")
    void getEnduringConsentWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                        client.getEnduringConsent(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank request ID is handled")
    void revokeEnduringConsentWithBlankRequestId(String requestId) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.revokeEnduringConsent(requestId, "accessToken", UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Request ID must not be blank");
    }

    @Test
    @DisplayName("Verify that null consent ID is handled")
    void revokeEnduringConsentWithNullConsentId() {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.revokeEnduringConsent(UUID.randomUUID().toString(), "accessToken", null).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Consent ID must not be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Verify that blank access token is handled")
    void revokeEnduringConsentWithBlankAccessToken(String accessToken) {
        IllegalArgumentException exception = catchThrowableOfType(() ->
                        client.revokeEnduringConsent(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                IllegalArgumentException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token must not be blank");
    }

    @Test
    @DisplayName("Verify that expired access token is handled")
    void revokeEnduringConsentWithExpiredAccessToken() {
        String accessToken = System.getenv("ACCESS_TOKEN");
        ExpiredAccessTokenException exception = catchThrowableOfType(() ->
                        client.revokeEnduringConsent(UUID.randomUUID().toString(), accessToken, UUID.randomUUID()).block(),
                ExpiredAccessTokenException.class);

        assertThat(exception)
                .isNotNull()
                .hasMessage("Access token has expired, generate a new one");
    }
}