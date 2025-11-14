package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.QuickPaymentRequest;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuickPaymentsApiClientTest {

    private QuickPaymentsApiClient client;

    @BeforeEach
    void setUp() {
        client = new QuickPaymentsApiClient(null);
    }

    // ===== createQuickPayment Validation Tests =====

    @Test
    void testCreateQuickPaymentWithNullRequest() {
        assertThatThrownBy(() -> client.createQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment request must not be null");
    }

    @Test
    void testCreateQuickPaymentWithNullRequestAndCustomRequestId() {
        assertThatThrownBy(() -> client.createQuickPayment(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment request must not be null");
    }

    @Test
    void testCreateQuickPaymentWithValidRequestThrowsNullPointerDueToNullHttpHelper() {
        QuickPaymentRequest request = new QuickPaymentRequest();

        assertThatThrownBy(() -> client.createQuickPayment(request))
                .isInstanceOf(NullPointerException.class);
    }

    // ===== getQuickPayment Validation Tests =====

    @Test
    void testGetQuickPaymentWithNullId() {
        assertThatThrownBy(() -> client.getQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment ID must not be null");
    }

    @Test
    void testGetQuickPaymentWithNullIdAndCustomRequestId() {
        assertThatThrownBy(() -> client.getQuickPayment(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment ID must not be null");
    }

    @Test
    void testGetQuickPaymentWithValidIdThrowsNullPointerDueToNullHttpHelper() {
        UUID validId = UUID.randomUUID();

        assertThatThrownBy(() -> client.getQuickPayment(validId))
                .isInstanceOf(NullPointerException.class);
    }

    // ===== revokeQuickPayment Validation Tests =====

    @Test
    void testRevokeQuickPaymentWithNullId() {
        assertThatThrownBy(() -> client.revokeQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment ID must not be null");
    }

    @Test
    void testRevokeQuickPaymentWithNullIdAndCustomRequestId() {
        assertThatThrownBy(() -> client.revokeQuickPayment(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment ID must not be null");
    }

    @Test
    void testRevokeQuickPaymentWithValidIdThrowsNullPointerDueToNullHttpHelper() {
        UUID validId = UUID.randomUUID();

        assertThatThrownBy(() -> client.revokeQuickPayment(validId))
                .isInstanceOf(NullPointerException.class);
    }
}
