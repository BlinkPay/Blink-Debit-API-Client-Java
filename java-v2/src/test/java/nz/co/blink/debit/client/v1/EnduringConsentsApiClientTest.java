package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnduringConsentsApiClientTest {

    private EnduringConsentsApiClient client;

    @BeforeEach
    void setUp() {
        client = new EnduringConsentsApiClient(null);
    }

    // ===== createEnduringConsent Validation Tests =====

    @Test
    void testCreateEnduringConsentWithNullRequest() {
        assertThatThrownBy(() -> client.createEnduringConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Enduring consent request must not be null");
    }

    @Test
    void testCreateEnduringConsentWithNullRequestAndCustomRequestId() {
        assertThatThrownBy(() -> client.createEnduringConsent(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Enduring consent request must not be null");
    }

    @Test
    void testCreateEnduringConsentWithValidRequestThrowsNullPointerDueToNullHttpHelper() {
        EnduringConsentRequest request = new EnduringConsentRequest();

        assertThatThrownBy(() -> client.createEnduringConsent(request))
                .isInstanceOf(NullPointerException.class);
    }

    // ===== getConsent Validation Tests =====

    @Test
    void testGetConsentWithNullId() {
        assertThatThrownBy(() -> client.getConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Consent ID must not be null");
    }

    @Test
    void testGetConsentWithNullIdAndCustomRequestId() {
        assertThatThrownBy(() -> client.getConsent(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Consent ID must not be null");
    }

    @Test
    void testGetConsentWithValidIdThrowsNullPointerDueToNullHttpHelper() {
        UUID validId = UUID.randomUUID();

        assertThatThrownBy(() -> client.getConsent(validId))
                .isInstanceOf(NullPointerException.class);
    }

    // ===== revokeConsent Validation Tests =====

    @Test
    void testRevokeConsentWithNullId() {
        assertThatThrownBy(() -> client.revokeConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Consent ID must not be null");
    }

    @Test
    void testRevokeConsentWithNullIdAndCustomRequestId() {
        assertThatThrownBy(() -> client.revokeConsent(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Consent ID must not be null");
    }

    @Test
    void testRevokeConsentWithValidIdThrowsNullPointerDueToNullHttpHelper() {
        UUID validId = UUID.randomUUID();

        assertThatThrownBy(() -> client.revokeConsent(validId))
                .isInstanceOf(NullPointerException.class);
    }
}
