package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.CreateQuickPaymentResponse;
import nz.co.blink.debit.dto.v1.QuickPaymentRequest;
import nz.co.blink.debit.dto.v1.QuickPaymentResponse;
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
class QuickPaymentsApiClientUnitTest {

    @Mock
    private HttpClientHelper httpHelper;

    private QuickPaymentsApiClient client;

    @BeforeEach
    void setUp() {
        client = new QuickPaymentsApiClient(httpHelper);
    }

    // ===== createQuickPayment Tests =====

    @Test
    void testCreateQuickPaymentSuccess() throws BlinkServiceException {
        QuickPaymentRequest request = mock(QuickPaymentRequest.class);
        CreateQuickPaymentResponse expectedResponse = mock(CreateQuickPaymentResponse.class);

        when(httpHelper.post(eq("/payments/v1/quick-payments"), eq(request), eq(CreateQuickPaymentResponse.class), anyString()))
                .thenReturn(expectedResponse);

        CreateQuickPaymentResponse result = client.createQuickPayment(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(httpHelper).post(eq("/payments/v1/quick-payments"), eq(request), eq(CreateQuickPaymentResponse.class), anyString());
    }

    @Test
    void testCreateQuickPaymentWithCustomRequestId() throws BlinkServiceException {
        QuickPaymentRequest request = mock(QuickPaymentRequest.class);
        CreateQuickPaymentResponse expectedResponse = mock(CreateQuickPaymentResponse.class);
        String customRequestId = "custom-request-id-123";

        when(httpHelper.post("/payments/v1/quick-payments", request, CreateQuickPaymentResponse.class, customRequestId))
                .thenReturn(expectedResponse);

        CreateQuickPaymentResponse result = client.createQuickPayment(request, customRequestId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(httpHelper).post("/payments/v1/quick-payments", request, CreateQuickPaymentResponse.class, customRequestId);
    }

    @Test
    void testCreateQuickPaymentWithNullRequest() throws BlinkServiceException {
        assertThatThrownBy(() -> client.createQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment request must not be null");

        verify(httpHelper, never()).post(anyString(), any(), any(), anyString());
    }

    @Test
    void testCreateQuickPaymentWithNullRequestAndCustomRequestId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.createQuickPayment(null, "custom-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment request must not be null");

        verify(httpHelper, never()).post(anyString(), any(), any(), anyString());
    }

    @Test
    void testCreateQuickPaymentHttpHelperThrowsException() throws BlinkServiceException {
        QuickPaymentRequest request = mock(QuickPaymentRequest.class);

        when(httpHelper.post(anyString(), any(), any(), anyString()))
                .thenThrow(new BlinkServiceException("API error"));

        assertThatThrownBy(() -> client.createQuickPayment(request))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageContaining("API error");
    }

    // ===== getQuickPayment Tests =====

    @Test
    void testGetQuickPaymentSuccess() throws BlinkServiceException {
        UUID quickPaymentId = UUID.randomUUID();
        QuickPaymentResponse expectedResponse = mock(QuickPaymentResponse.class);

        when(httpHelper.get(eq("/payments/v1/quick-payments/" + quickPaymentId.toString()),
                eq(QuickPaymentResponse.class), anyString()))
                .thenReturn(expectedResponse);

        QuickPaymentResponse result = client.getQuickPayment(quickPaymentId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(httpHelper).get(eq("/payments/v1/quick-payments/" + quickPaymentId.toString()),
                eq(QuickPaymentResponse.class), anyString());
    }

    @Test
    void testGetQuickPaymentWithCustomRequestId() throws BlinkServiceException {
        UUID quickPaymentId = UUID.randomUUID();
        QuickPaymentResponse expectedResponse = mock(QuickPaymentResponse.class);
        String customRequestId = "get-request-id";

        when(httpHelper.get("/payments/v1/quick-payments/" + quickPaymentId.toString(),
                QuickPaymentResponse.class, customRequestId))
                .thenReturn(expectedResponse);

        QuickPaymentResponse result = client.getQuickPayment(quickPaymentId, customRequestId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(httpHelper).get("/payments/v1/quick-payments/" + quickPaymentId.toString(),
                QuickPaymentResponse.class, customRequestId);
    }

    @Test
    void testGetQuickPaymentWithNullId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.getQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment ID must not be null");

        verify(httpHelper, never()).get(anyString(), any(), anyString());
    }

    @Test
    void testGetQuickPaymentWithNullIdAndCustomRequestId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.getQuickPayment(null, "custom-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment ID must not be null");

        verify(httpHelper, never()).get(anyString(), any(), anyString());
    }

    @Test
    void testGetQuickPaymentPathConstruction() throws BlinkServiceException {
        UUID quickPaymentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        QuickPaymentResponse mockResponse = mock(QuickPaymentResponse.class);

        when(httpHelper.get(anyString(), eq(QuickPaymentResponse.class), anyString()))
                .thenReturn(mockResponse);

        client.getQuickPayment(quickPaymentId);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpHelper).get(pathCaptor.capture(), eq(QuickPaymentResponse.class), anyString());

        assertThat(pathCaptor.getValue()).isEqualTo("/payments/v1/quick-payments/123e4567-e89b-12d3-a456-426614174000");
    }

    // ===== revokeQuickPayment Tests =====

    @Test
    void testRevokeQuickPaymentSuccess() throws BlinkServiceException {
        UUID quickPaymentId = UUID.randomUUID();

        doNothing().when(httpHelper).delete(eq("/payments/v1/quick-payments/" + quickPaymentId.toString()), anyString());

        client.revokeQuickPayment(quickPaymentId);

        verify(httpHelper).delete(eq("/payments/v1/quick-payments/" + quickPaymentId.toString()), anyString());
    }

    @Test
    void testRevokeQuickPaymentWithCustomRequestId() throws BlinkServiceException {
        UUID quickPaymentId = UUID.randomUUID();
        String customRequestId = "revoke-request-id";

        doNothing().when(httpHelper).delete("/payments/v1/quick-payments/" + quickPaymentId.toString(), customRequestId);

        client.revokeQuickPayment(quickPaymentId, customRequestId);

        verify(httpHelper).delete("/payments/v1/quick-payments/" + quickPaymentId.toString(), customRequestId);
    }

    @Test
    void testRevokeQuickPaymentWithNullId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.revokeQuickPayment(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment ID must not be null");

        verify(httpHelper, never()).delete(anyString(), anyString());
    }

    @Test
    void testRevokeQuickPaymentWithNullIdAndCustomRequestId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.revokeQuickPayment(null, "custom-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Quick payment ID must not be null");

        verify(httpHelper, never()).delete(anyString(), anyString());
    }

    @Test
    void testRevokeQuickPaymentPathConstruction() throws BlinkServiceException {
        UUID quickPaymentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        doNothing().when(httpHelper).delete(anyString(), anyString());

        client.revokeQuickPayment(quickPaymentId);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpHelper).delete(pathCaptor.capture(), anyString());

        assertThat(pathCaptor.getValue()).isEqualTo("/payments/v1/quick-payments/123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void testRevokeQuickPaymentHttpHelperThrowsException() throws BlinkServiceException {
        UUID quickPaymentId = UUID.randomUUID();

        doThrow(new BlinkServiceException("Delete failed"))
                .when(httpHelper).delete(anyString(), anyString());

        assertThatThrownBy(() -> client.revokeQuickPayment(quickPaymentId))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageContaining("Delete failed");
    }
}
