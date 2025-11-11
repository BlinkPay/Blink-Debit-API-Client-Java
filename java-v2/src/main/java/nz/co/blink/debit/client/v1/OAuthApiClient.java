package nz.co.blink.debit.client.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.co.blink.debit.config.BlinkDebitConfig;
import nz.co.blink.debit.dto.v1.AccessTokenRequest;
import nz.co.blink.debit.dto.v1.AccessTokenResponse;
import nz.co.blink.debit.exception.BlinkServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Client for OAuth2 token operations.
 * Uses Java 11+ HttpClient for synchronous HTTP calls.
 */
public class OAuthApiClient {

    private static final Logger log = LoggerFactory.getLogger(OAuthApiClient.class);
    private static final String TOKEN_PATH = "/oauth2/token";

    private final HttpClient httpClient;
    private final BlinkDebitConfig config;
    private final ObjectMapper objectMapper;
    private final String tokenUrl;

    public OAuthApiClient(HttpClient httpClient, BlinkDebitConfig config, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.config = config;
        this.objectMapper = objectMapper;
        this.tokenUrl = config.getDebitUrl() + TOKEN_PATH;
    }

    /**
     * Get an access token using client credentials flow.
     * Synchronous blocking call.
     *
     * @return the access token string
     * @throws BlinkServiceException if the request fails
     */
    public String getAccessToken() throws BlinkServiceException {
        return getAccessToken(UUID.randomUUID().toString());
    }

    /**
     * Get an access token using client credentials flow.
     * Synchronous blocking call.
     *
     * @param requestId the request ID for tracing
     * @return the access token string
     * @throws BlinkServiceException if the request fails
     */
    public String getAccessToken(String requestId) throws BlinkServiceException {
        try {
            AccessTokenRequest tokenRequest = AccessTokenRequest.builder()
                    .clientId(config.getClientId())
                    .clientSecret(config.getClientSecret())
                    .grantType("client_credentials")
                    .build();

            String requestBody = objectMapper.writeValueAsString(tokenRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .timeout(config.getTimeout())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("request-id", requestId)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            log.debug("Requesting access token from {}", tokenUrl);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                AccessTokenResponse tokenResponse = objectMapper.readValue(response.body(), AccessTokenResponse.class);
                log.debug("Successfully obtained access token");
                return tokenResponse.getAccessToken();
            } else {
                String errorMessage = String.format("Failed to get access token: HTTP %d - %s",
                        response.statusCode(), response.body());
                log.error(errorMessage);
                throw new BlinkServiceException(errorMessage);
            }

        } catch (IOException e) {
            log.error("IO error while getting access token", e);
            throw new BlinkServiceException("IO error while getting access token", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while getting access token", e);
            throw new BlinkServiceException("Interrupted while getting access token", e);
        }
    }
}
