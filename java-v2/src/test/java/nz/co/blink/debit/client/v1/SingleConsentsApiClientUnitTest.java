package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.SingleConsentRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SingleConsentsApiClientUnitTest {

    @Mock
    private HttpClientHelper httpHelper;

    private SingleConsentsApiClient client;

    @BeforeEach
    void setUp() {
        client = new SingleConsentsApiClient(httpHelper);
    }

    // ===== createSingleConsent Tests =====

    @Test
    void testCreateSingleConsentSuccess() throws BlinkServiceException {
        SingleConsentRequest request = mock(SingleConsentRequest.class);
        CreateConsentResponse expectedResponse = mock(CreateConsentResponse.class);

        when(httpHelper.post(eq("/consents/single"), eq(request), eq(CreateConsentResponse.class), anyString()))
                .thenReturn(expectedResponse);

        CreateConsentResponse result = client.createSingleConsent(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(httpHelper).post(eq("/consents/single"), eq(request), eq(CreateConsentResponse.class), anyString());
    }

    @Test
    void testCreateSingleConsentWithCustomRequestId() throws BlinkServiceException {
        SingleConsentRequest request = mock(SingleConsentRequest.class);
        CreateConsentResponse expectedResponse = mock(CreateConsentResponse.class);
        String customRequestId = "custom-request-id-123";

        when(httpHelper.post("/consents/single", request, CreateConsentResponse.class, customRequestId))
                .thenReturn(expectedResponse);

        CreateConsentResponse result = client.createSingleConsent(request, customRequestId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(httpHelper).post("/consents/single", request, CreateConsentResponse.class, customRequestId);
    }

    @Test
    void testCreateSingleConsentWithNullRequest() throws BlinkServiceException {
        assertThatThrownBy(() -> client.createSingleConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Single consent request must not be null");

        verify(httpHelper, never()).post(anyString(), any(), any(), anyString());
    }

    @Test
    void testCreateSingleConsentWithNullRequestAndCustomRequestId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.createSingleConsent(null, "custom-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Single consent request must not be null");

        verify(httpHelper, never()).post(anyString(), any(), any(), anyString());
    }

    @Test
    void testCreateSingleConsentHttpHelperThrowsException() throws BlinkServiceException {
        SingleConsentRequest request = mock(SingleConsentRequest.class);

        when(httpHelper.post(anyString(), any(), any(), anyString()))
                .thenThrow(new BlinkServiceException("API error"));

        assertThatThrownBy(() -> client.createSingleConsent(request))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageContaining("API error");
    }

    // ===== getConsent Tests =====

    @Test
    void testGetConsentSuccess() throws BlinkServiceException {
        UUID consentId = UUID.randomUUID();
        Consent expectedConsent = mock(Consent.class);

        when(httpHelper.get(eq("/consents/" + consentId.toString()), eq(Consent.class), anyString()))
                .thenReturn(expectedConsent);

        Consent result = client.getConsent(consentId);

        assertThat(result).isEqualTo(expectedConsent);
        verify(httpHelper).get(eq("/consents/" + consentId.toString()), eq(Consent.class), anyString());
    }

    @Test
    void testGetConsentWithCustomRequestId() throws BlinkServiceException {
        UUID consentId = UUID.randomUUID();
        Consent expectedConsent = mock(Consent.class);
        String customRequestId = "get-request-id";

        when(httpHelper.get("/consents/" + consentId.toString(), Consent.class, customRequestId))
                .thenReturn(expectedConsent);

        Consent result = client.getConsent(consentId, customRequestId);

        assertThat(result).isEqualTo(expectedConsent);
        verify(httpHelper).get("/consents/" + consentId.toString(), Consent.class, customRequestId);
    }

    @Test
    void testGetConsentWithNullId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.getConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Consent ID must not be null");

        verify(httpHelper, never()).get(anyString(), any(), anyString());
    }

    @Test
    void testGetConsentWithNullIdAndCustomRequestId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.getConsent(null, "custom-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Consent ID must not be null");

        verify(httpHelper, never()).get(anyString(), any(), anyString());
    }

    @Test
    void testGetConsentPathConstruction() throws BlinkServiceException {
        UUID consentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Consent mockConsent = mock(Consent.class);

        when(httpHelper.get(anyString(), eq(Consent.class), anyString()))
                .thenReturn(mockConsent);

        client.getConsent(consentId);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpHelper).get(pathCaptor.capture(), eq(Consent.class), anyString());

        assertThat(pathCaptor.getValue()).isEqualTo("/consents/123e4567-e89b-12d3-a456-426614174000");
    }

    // ===== revokeConsent Tests =====

    @Test
    void testRevokeConsentSuccess() throws BlinkServiceException {
        UUID consentId = UUID.randomUUID();

        doNothing().when(httpHelper).delete(eq("/consents/" + consentId.toString()), anyString());

        client.revokeConsent(consentId);

        verify(httpHelper).delete(eq("/consents/" + consentId.toString()), anyString());
    }

    @Test
    void testRevokeConsentWithCustomRequestId() throws BlinkServiceException {
        UUID consentId = UUID.randomUUID();
        String customRequestId = "revoke-request-id";

        doNothing().when(httpHelper).delete("/consents/" + consentId.toString(), customRequestId);

        client.revokeConsent(consentId, customRequestId);

        verify(httpHelper).delete("/consents/" + consentId.toString(), customRequestId);
    }

    @Test
    void testRevokeConsentWithNullId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.revokeConsent(null))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Consent ID must not be null");

        verify(httpHelper, never()).delete(anyString(), anyString());
    }

    @Test
    void testRevokeConsentWithNullIdAndCustomRequestId() throws BlinkServiceException {
        assertThatThrownBy(() -> client.revokeConsent(null, "custom-id"))
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Consent ID must not be null");

        verify(httpHelper, never()).delete(anyString(), anyString());
    }

    @Test
    void testRevokeConsentPathConstruction() throws BlinkServiceException {
        UUID consentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        doNothing().when(httpHelper).delete(anyString(), anyString());

        client.revokeConsent(consentId);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpHelper).delete(pathCaptor.capture(), anyString());

        assertThat(pathCaptor.getValue()).isEqualTo("/consents/123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void testRevokeConsentHttpHelperThrowsException() throws BlinkServiceException {
        UUID consentId = UUID.randomUUID();

        doThrow(new BlinkServiceException("Delete failed"))
                .when(httpHelper).delete(anyString(), anyString());

        assertThatThrownBy(() -> client.revokeConsent(consentId))
                .isInstanceOf(BlinkServiceException.class)
                .hasMessageContaining("Delete failed");
    }
}
