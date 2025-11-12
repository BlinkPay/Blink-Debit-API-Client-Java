package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.HttpClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsApiClientUnitTest {

    @Mock
    private HttpClientHelper httpHelper;

    private PaymentsApiClient client;

    @BeforeEach
    void setUp() {
        client = new PaymentsApiClient(httpHelper);
    }

    // ===== createPayment Tests =====

    @Test
    void testCreatePaymentSuccess() throws BlinkServiceException {
        PaymentRequest request = mock(PaymentRequest.class);
        PaymentResponse expectedResponse = mock(PaymentResponse.class);

        when(httpHelper.post(eq("/payments"), eq(request), eq(PaymentResponse.class), anyString()))
                .thenReturn(expectedResponse);

        PaymentResponse result = client.createPayment(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(httpHelper).post(eq("/payments"), eq(request), eq(PaymentResponse.class), anyString());
    }

    @Test
    void testCreatePaymentWithCustomRequestId() throws BlinkServiceException {
        PaymentRequest request = mock(PaymentRequest.class);
        PaymentResponse expectedResponse = mock(PaymentResponse.class);
        String customRequestId = "custom-request-id-123";

        when(httpHelper.post("/payments", request, PaymentResponse.class, customRequestId))
                .thenReturn(expectedResponse);

        PaymentResponse result = client.createPayment(request, customRequestId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(httpHelper).post("/payments", request, PaymentResponse.class, customRequestId);
    }

    @Test
    void testCreatePaymentWithNullRequest() throws BlinkServiceException {
        assertThatThrownBy(() -> client.createPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Payment request must not be null");

        verify(httpHelper, never()).post(anyString(), any(), any(), anyString());
    }

    @Test
    void testCreatePaymentWithNullRequestAndCustomRequestId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.createPayment(null, "custom-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Payment request must not be null");

        verify(httpHelper, never()).post(anyString(), any(), any(), anyString());
    }

    @Test
    void testCreatePaymentHttpHelperThrowsException() throws BlinkServiceException {
        PaymentRequest request = mock(PaymentRequest.class);

        when(httpHelper.post(anyString(), any(), any(), anyString()))
                .thenThrow(new BlinkServiceException("API error"));

        assertThatThrownBy(() -> client.createPayment(request))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageContaining("API error");
    }

    // ===== getPayment Tests =====

    @Test
    void testGetPaymentSuccess() throws BlinkServiceException {
        UUID paymentId = UUID.randomUUID();
        Payment expectedPayment = mock(Payment.class);

        when(httpHelper.get(eq("/payments/" + paymentId.toString()), eq(Payment.class), anyString()))
                .thenReturn(expectedPayment);

        Payment result = client.getPayment(paymentId);

        assertThat(result).isEqualTo(expectedPayment);
        verify(httpHelper).get(eq("/payments/" + paymentId.toString()), eq(Payment.class), anyString());
    }

    @Test
    void testGetPaymentWithCustomRequestId() throws BlinkServiceException {
        UUID paymentId = UUID.randomUUID();
        Payment expectedPayment = mock(Payment.class);
        String customRequestId = "get-request-id";

        when(httpHelper.get("/payments/" + paymentId.toString(), Payment.class, customRequestId))
                .thenReturn(expectedPayment);

        Payment result = client.getPayment(paymentId, customRequestId);

        assertThat(result).isEqualTo(expectedPayment);
        verify(httpHelper).get("/payments/" + paymentId.toString(), Payment.class, customRequestId);
    }

    @Test
    void testGetPaymentWithNullId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.getPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Payment ID must not be null");

        verify(httpHelper, never()).get(anyString(), any(), anyString());
    }

    @Test
    void testGetPaymentWithNullIdAndCustomRequestId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.getPayment(null, "custom-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Payment ID must not be null");

        verify(httpHelper, never()).get(anyString(), any(), anyString());
    }

    @Test
    void testGetPaymentPathConstruction() throws BlinkServiceException {
        UUID paymentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Payment mockPayment = mock(Payment.class);

        when(httpHelper.get(anyString(), eq(Payment.class), anyString()))
                .thenReturn(mockPayment);

        client.getPayment(paymentId);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpHelper).get(pathCaptor.capture(), eq(Payment.class), anyString());

        assertThat(pathCaptor.getValue()).isEqualTo("/payments/123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void testGetPaymentHttpHelperThrowsException() throws BlinkServiceException {
        UUID paymentId = UUID.randomUUID();

        when(httpHelper.get(anyString(), any(), anyString()))
                .thenThrow(new BlinkServiceException("API error"));

        assertThatThrownBy(() -> client.getPayment(paymentId))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageContaining("API error");
    }
}
