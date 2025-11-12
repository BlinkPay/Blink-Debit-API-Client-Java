package nz.co.blink.debit.config;

import nz.co.blink.debit.exception.BlinkInvalidValueException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlinkDebitConfigTest {

    private static final String VALID_URL = "https://staging.debit.blinkpay.co.nz";
    private static final String VALID_CLIENT_ID = "test-client-id";
    private static final String VALID_CLIENT_SECRET = "test-client-secret";

    // ===== Builder Pattern Tests =====

    @Test
    void testBuildWithAllRequiredFields() throws BlinkInvalidValueException {
        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .build();

        assertThat(config.getDebitUrl()).isEqualTo(VALID_URL);
        assertThat(config.getClientId()).isEqualTo(VALID_CLIENT_ID);
        assertThat(config.getClientSecret()).isEqualTo(VALID_CLIENT_SECRET);
    }

    @Test
    void testBuildWithDefaultTimeout() throws BlinkInvalidValueException {
        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .build();

        assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void testBuildWithCustomTimeout() throws BlinkInvalidValueException {
        Duration customTimeout = Duration.ofSeconds(60);

        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .timeout(customTimeout)
                .build();

        assertThat(config.getTimeout()).isEqualTo(customTimeout);
    }

    @Test
    void testBuildWithDefaultConnectionPoolSize() throws BlinkInvalidValueException {
        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .build();

        assertThat(config.getConnectionPoolSize()).isEqualTo(5);
    }

    @Test
    void testBuildWithCustomConnectionPoolSize() throws BlinkInvalidValueException {
        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .connectionPoolSize(10)
                .build();

        assertThat(config.getConnectionPoolSize()).isEqualTo(10);
    }

    @Test
    void testBuildWithZeroOrNegativeConnectionPoolSizeUsesDefault() throws BlinkInvalidValueException {
        BlinkDebitConfig configZero = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .connectionPoolSize(0)
                .build();

        assertThat(configZero.getConnectionPoolSize()).isEqualTo(5);

        BlinkDebitConfig configNegative = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .connectionPoolSize(-1)
                .build();

        assertThat(configNegative.getConnectionPoolSize()).isEqualTo(5);
    }

    @Test
    void testBuildWithRetryEnabled() throws BlinkInvalidValueException {
        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .retryEnabled(true)
                .build();

        assertThat(config.isRetryEnabled()).isTrue();
    }

    @Test
    void testBuildWithRetryDisabled() throws BlinkInvalidValueException {
        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .retryEnabled(false)
                .build();

        assertThat(config.isRetryEnabled()).isFalse();
    }

    @Test
    void testBuildWithDefaultRetryEnabled() throws BlinkInvalidValueException {
        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .build();

        assertThat(config.isRetryEnabled()).isTrue();
    }

    // ===== Validation Tests =====

    @Test
    void testBuildWithNullDebitUrl() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl(null)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit URL is not configured");
    }

    @Test
    void testBuildWithEmptyDebitUrl() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl("")
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit URL is not configured");
    }

    @Test
    void testBuildWithWhitespaceDebitUrl() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl("   ")
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit URL is not configured");
    }

    @Test
    void testBuildWithNullClientId() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(null)
                .clientSecret(VALID_CLIENT_SECRET)
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit client ID is not configured");
    }

    @Test
    void testBuildWithEmptyClientId() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId("")
                .clientSecret(VALID_CLIENT_SECRET)
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit client ID is not configured");
    }

    @Test
    void testBuildWithWhitespaceClientId() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId("   ")
                .clientSecret(VALID_CLIENT_SECRET)
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit client ID is not configured");
    }

    @Test
    void testBuildWithNullClientSecret() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(null)
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit client secret is not configured");
    }

    @Test
    void testBuildWithEmptyClientSecret() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret("")
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit client secret is not configured");
    }

    @Test
    void testBuildWithWhitespaceClientSecret() {
        assertThatThrownBy(() -> BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret("   ")
                .build())
                .isInstanceOf(BlinkInvalidValueException.class)
                .hasMessageContaining("Blink Debit client secret is not configured");
    }

    // ===== Immutability Tests =====

    @Test
    void testConfigIsImmutable() throws BlinkInvalidValueException {
        Duration customTimeout = Duration.ofSeconds(45);

        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .timeout(customTimeout)
                .connectionPoolSize(8)
                .retryEnabled(false)
                .build();

        // All getters should return same values
        assertThat(config.getDebitUrl()).isEqualTo(VALID_URL);
        assertThat(config.getClientId()).isEqualTo(VALID_CLIENT_ID);
        assertThat(config.getClientSecret()).isEqualTo(VALID_CLIENT_SECRET);
        assertThat(config.getTimeout()).isEqualTo(customTimeout);
        assertThat(config.getConnectionPoolSize()).isEqualTo(8);
        assertThat(config.isRetryEnabled()).isFalse();

        // Multiple calls return same values
        assertThat(config.getDebitUrl()).isEqualTo(VALID_URL);
        assertThat(config.getTimeout()).isEqualTo(customTimeout);
    }

    @Test
    void testBuilderIsReusable() throws BlinkInvalidValueException {
        BlinkDebitConfig.Builder builder = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET);

        BlinkDebitConfig config1 = builder.build();
        BlinkDebitConfig config2 = builder.timeout(Duration.ofSeconds(60)).build();

        assertThat(config1).isNotNull();
        assertThat(config2).isNotNull();
        assertThat(config1.getTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config2.getTimeout()).isEqualTo(Duration.ofSeconds(60));
    }

    // ===== Builder Chaining Tests =====

    @Test
    void testBuilderChaining() throws BlinkInvalidValueException {
        BlinkDebitConfig config = BlinkDebitConfig.builder()
                .debitUrl(VALID_URL)
                .clientId(VALID_CLIENT_ID)
                .clientSecret(VALID_CLIENT_SECRET)
                .timeout(Duration.ofSeconds(120))
                .connectionPoolSize(15)
                .retryEnabled(false)
                .build();

        assertThat(config.getDebitUrl()).isEqualTo(VALID_URL);
        assertThat(config.getClientId()).isEqualTo(VALID_CLIENT_ID);
        assertThat(config.getClientSecret()).isEqualTo(VALID_CLIENT_SECRET);
        assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(120));
        assertThat(config.getConnectionPoolSize()).isEqualTo(15);
        assertThat(config.isRetryEnabled()).isFalse();
    }
}
