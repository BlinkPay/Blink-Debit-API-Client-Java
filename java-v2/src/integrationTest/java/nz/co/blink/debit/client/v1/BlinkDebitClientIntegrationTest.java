package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.config.BlinkDebitConfig;
import nz.co.blink.debit.dto.v1.BankMetadata;
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import nz.co.blink.debit.exception.BlinkServiceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for BlinkDebitClient.
 * Requires real API credentials via environment variables.
 */
@Tag("integration")
class BlinkDebitClientIntegrationTest {

    private static BlinkDebitClient client;
    private static boolean credentialsAvailable;

    @BeforeAll
    static void setUp() throws BlinkInvalidValueException {
        String debitUrl = System.getenv("BLINKPAY_DEBIT_URL");
        String clientId = System.getenv("BLINKPAY_CLIENT_ID");
        String clientSecret = System.getenv("BLINKPAY_CLIENT_SECRET");

        credentialsAvailable = debitUrl != null && clientId != null && clientSecret != null;

        if (credentialsAvailable) {
            BlinkDebitConfig config = BlinkDebitConfig.builder()
                    .debitUrl(debitUrl)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            client = new BlinkDebitClient(config);
        }
    }

    @AfterAll
    static void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    void testGetMetadata() throws BlinkServiceException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        List<BankMetadata> metadata = client.getMetaApi().getMeta();

        assertThat(metadata).isNotNull();
        assertThat(metadata).isNotEmpty();
        assertThat(metadata.get(0).getName()).isNotNull();
    }

    @Test
    void testClientFromEnvironment() throws BlinkInvalidValueException {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        try (BlinkDebitClient envClient = new BlinkDebitClient()) {
            assertThat(envClient).isNotNull();
            assertThat(envClient.getConfig()).isNotNull();
            assertThat(envClient.getConfig().getDebitUrl()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void testGetApiClients() {
        assumeTrue(credentialsAvailable, "Integration test credentials not available");

        assertThat(client.getSingleConsentsApi()).isNotNull();
        assertThat(client.getEnduringConsentsApi()).isNotNull();
        assertThat(client.getQuickPaymentsApi()).isNotNull();
        assertThat(client.getPaymentsApi()).isNotNull();
        assertThat(client.getRefundsApi()).isNotNull();
        assertThat(client.getMetaApi()).isNotNull();
    }
}
