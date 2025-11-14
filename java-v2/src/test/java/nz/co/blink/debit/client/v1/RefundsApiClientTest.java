package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.AccountNumberRefundRequest;
import nz.co.blink.debit.dto.v1.RefundDetail;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundsApiClientTest {

    private RefundsApiClient client;

    @BeforeEach
    void setUp() {
        client = new RefundsApiClient(null);
    }

    // ===== createRefund Validation Tests =====

    @Test
    void testCreateRefundWithNullRequest() {
        assertThatThrownBy(() -> client.createRefund(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Refund request must not be null");
    }

    @Test
    void testCreateRefundWithNullRequestAndCustomRequestId() {
        assertThatThrownBy(() -> client.createRefund(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Refund request must not be null");
    }

    @Test
    void testCreateRefundWithValidRequestThrowsNullPointerDueToNullHttpHelper() {
        // Use AccountNumberRefundRequest as a concrete implementation of RefundDetail
        RefundDetail request = new AccountNumberRefundRequest(UUID.randomUUID());

        assertThatThrownBy(() -> client.createRefund(request))
                .isInstanceOf(NullPointerException.class);
    }

    // ===== getRefund Validation Tests =====

    @Test
    void testGetRefundWithNullId() {
        assertThatThrownBy(() -> client.getRefund(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Refund ID must not be null");
    }

    @Test
    void testGetRefundWithNullIdAndCustomRequestId() {
        assertThatThrownBy(() -> client.getRefund(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Refund ID must not be null");
    }

    @Test
    void testGetRefundWithValidIdThrowsNullPointerDueToNullHttpHelper() {
        UUID validId = UUID.randomUUID();

        assertThatThrownBy(() -> client.getRefund(validId))
                .isInstanceOf(NullPointerException.class);
    }
}
