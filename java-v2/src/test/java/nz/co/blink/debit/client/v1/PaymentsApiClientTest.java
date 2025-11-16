package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentsApiClientTest {

    private PaymentsApiClient client;

    @BeforeEach
    void setUp() {
        client = new PaymentsApiClient(null);
    }

    // ===== createPayment Validation Tests =====

    @Test
    void testCreatePaymentWithNullRequest() {
        assertThatThrownBy(() -> client.createPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Payment request must not be null");
    }

    @Test
    void testCreatePaymentWithNullRequestAndCustomRequestId() {
        assertThatThrownBy(() -> client.createPayment(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Payment request must not be null");
    }

    @Test
    void testCreatePaymentWithValidRequestThrowsNullPointerDueToNullHttpHelper() {
        PaymentRequest request = new PaymentRequest();

        assertThatThrownBy(() -> client.createPayment(request))
                .isInstanceOf(NullPointerException.class);
    }

    // ===== getPayment Validation Tests =====

    @Test
    void testGetPaymentWithNullId() {
        assertThatThrownBy(() -> client.getPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Payment ID must not be null");
    }

    @Test
    void testGetPaymentWithNullIdAndCustomRequestId() {
        assertThatThrownBy(() -> client.getPayment(null, "custom-request-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Payment ID must not be null");
    }

    @Test
    void testGetPaymentWithValidIdThrowsNullPointerDueToNullHttpHelper() {
        UUID validId = UUID.randomUUID();

        assertThatThrownBy(() -> client.getPayment(validId))
                .isInstanceOf(NullPointerException.class);
    }
}
