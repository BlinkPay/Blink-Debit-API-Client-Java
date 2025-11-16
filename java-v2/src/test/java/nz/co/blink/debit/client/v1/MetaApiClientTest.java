package nz.co.blink.debit.client.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetaApiClientTest {

    private MetaApiClient client;

    @BeforeEach
    void setUp() {
        client = new MetaApiClient(null);
    }

    // ===== getMeta Validation Tests =====

    @Test
    void testGetMetaThrowsNullPointerDueToNullHttpHelper() {
        assertThatThrownBy(() -> client.getMeta())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testGetMetaWithCustomRequestIdThrowsNullPointerDueToNullHttpHelper() {
        assertThatThrownBy(() -> client.getMeta("custom-request-id"))
                .isInstanceOf(NullPointerException.class);
    }
}
