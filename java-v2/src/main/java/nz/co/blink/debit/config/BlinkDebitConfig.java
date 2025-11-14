package nz.co.blink.debit.config;

import nz.co.blink.debit.exception.BlinkInvalidValueException;

import java.time.Duration;

/**
 * Immutable configuration for Blink Debit API client.
 * Uses builder pattern for flexible construction with sensible defaults.
 */
public final class BlinkDebitConfig {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_CONNECTION_POOL_SIZE = 5;

    private final String debitUrl;
    private final String clientId;
    private final String clientSecret;
    private final Duration timeout;
    private final int connectionPoolSize;
    private final boolean retryEnabled;

    private BlinkDebitConfig(Builder builder) throws BlinkInvalidValueException {
        this.debitUrl = builder.debitUrl;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.timeout = builder.timeout != null ? builder.timeout : Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS);
        this.connectionPoolSize = builder.connectionPoolSize > 0 ? builder.connectionPoolSize : DEFAULT_CONNECTION_POOL_SIZE;
        this.retryEnabled = builder.retryEnabled;

        validate();
    }

    private void validate() throws BlinkInvalidValueException {
        if (debitUrl == null || debitUrl.trim().isEmpty()) {
            throw new BlinkInvalidValueException("Blink Debit URL is not configured");
        }
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new BlinkInvalidValueException("Blink Debit client ID is not configured");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            throw new BlinkInvalidValueException("Blink Debit client secret is not configured");
        }
    }

    public String getDebitUrl() {
        return debitUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public boolean isRetryEnabled() {
        return retryEnabled;
    }

    /**
     * Create a builder from environment variables.
     * Looks for: BLINKPAY_DEBIT_URL, BLINKPAY_CLIENT_ID, BLINKPAY_CLIENT_SECRET
     */
    public static Builder fromEnvironment() {
        return new Builder()
                .debitUrl(System.getenv("BLINKPAY_DEBIT_URL"))
                .clientId(System.getenv("BLINKPAY_CLIENT_ID"))
                .clientSecret(System.getenv("BLINKPAY_CLIENT_SECRET"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String debitUrl;
        private String clientId;
        private String clientSecret;
        private Duration timeout;
        private int connectionPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
        private boolean retryEnabled = true;

        public Builder debitUrl(String debitUrl) {
            this.debitUrl = debitUrl;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder connectionPoolSize(int connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
            return this;
        }

        public Builder retryEnabled(boolean retryEnabled) {
            this.retryEnabled = retryEnabled;
            return this;
        }

        public BlinkDebitConfig build() throws BlinkInvalidValueException {
            return new BlinkDebitConfig(this);
        }
    }
}
