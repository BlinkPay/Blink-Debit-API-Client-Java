package nz.co.blink.debit.client.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;

/**
 * Base class for component tests that require API credentials for OAuth.
 * Tests will be skipped if credentials are not available.
 */
public abstract class BaseComponentTest {

    @BeforeEach
    void checkCredentialsForOAuth() {
        String clientId = System.getenv("BLINKPAY_CLIENT_ID");
        String clientSecret = System.getenv("BLINKPAY_CLIENT_SECRET");

        // Component tests with setUp() methods need credentials for OAuth
        if (this.getClass().getDeclaredMethods().length > 0) {
            try {
                this.getClass().getDeclaredMethod("setUp");
                Assumptions.assumeTrue(
                    clientId != null && !clientId.isEmpty() &&
                    clientSecret != null && !clientSecret.isEmpty(),
                    "Skipping component test - OAuth credentials not available"
                );
            } catch (NoSuchMethodException e) {
                // No setUp method, test doesn't need credentials
            }
        }
    }
}