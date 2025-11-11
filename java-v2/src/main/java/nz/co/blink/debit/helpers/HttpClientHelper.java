package nz.co.blink.debit.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.co.blink.debit.config.BlinkDebitConfig;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.service.AccessTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Helper class for HTTP operations with Blink Debit API.
 * Handles common concerns like authentication, serialization, and error handling.
 */
public class HttpClientHelper {

    private static final Logger log = LoggerFactory.getLogger(HttpClientHelper.class);

    private final HttpClient httpClient;
    private final BlinkDebitConfig config;
    private final ObjectMapper objectMapper;
    private final AccessTokenManager tokenManager;
    private final String baseUrl;

    public HttpClientHelper(HttpClient httpClient, BlinkDebitConfig config,
                          ObjectMapper objectMapper, AccessTokenManager tokenManager) {
        this.httpClient = httpClient;
        this.config = config;
        this.objectMapper = objectMapper;
        this.tokenManager = tokenManager;
        this.baseUrl = config.getDebitUrl();
    }

    /**
     * Execute a POST request with JSON body.
     */
    public <T, R> R post(String path, T requestBody, Class<R> responseType) throws BlinkServiceException {
        return post(path, requestBody, responseType, UUID.randomUUID().toString());
    }

    /**
     * Execute a POST request with JSON body and custom request ID.
     */
    public <T, R> R post(String path, T requestBody, Class<R> responseType, String requestId)
            throws BlinkServiceException {
        try {
            String accessToken = tokenManager.getAccessToken();
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(config.getTimeout())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("request-id", requestId)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            log.debug("POST {} with request-id: {}", path, requestId);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, responseType);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request body", e);
            throw new BlinkServiceException("Failed to serialize request body", e);
        } catch (IOException e) {
            log.error("IO error during POST request to {}", path, e);
            throw new BlinkServiceException("IO error during POST request to " + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted during POST request to {}", path, e);
            throw new BlinkServiceException("Interrupted during POST request to " + path, e);
        }
    }

    /**
     * Execute a GET request.
     */
    public <R> R get(String path, Class<R> responseType) throws BlinkServiceException {
        return get(path, responseType, UUID.randomUUID().toString());
    }

    /**
     * Execute a GET request with custom request ID.
     */
    public <R> R get(String path, Class<R> responseType, String requestId) throws BlinkServiceException {
        try {
            String accessToken = tokenManager.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(config.getTimeout())
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("request-id", requestId)
                    .GET()
                    .build();

            log.debug("GET {} with request-id: {}", path, requestId);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, responseType);

        } catch (IOException e) {
            log.error("IO error during GET request to {}", path, e);
            throw new BlinkServiceException("IO error during GET request to " + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted during GET request to {}", path, e);
            throw new BlinkServiceException("Interrupted during GET request to " + path, e);
        }
    }

    /**
     * Execute a DELETE request.
     */
    public void delete(String path) throws BlinkServiceException {
        delete(path, UUID.randomUUID().toString());
    }

    /**
     * Execute a DELETE request with custom request ID.
     */
    public void delete(String path, String requestId) throws BlinkServiceException {
        try {
            String accessToken = tokenManager.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(config.getTimeout())
                    .header("Authorization", "Bearer " + accessToken)
                    .header("request-id", requestId)
                    .DELETE()
                    .build();

            log.debug("DELETE {} with request-id: {}", path, requestId);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorMessage = String.format("DELETE request failed: HTTP %d - %s",
                        response.statusCode(), response.body());
                log.error(errorMessage);
                throw new BlinkServiceException(errorMessage);
            }

        } catch (IOException e) {
            log.error("IO error during DELETE request to {}", path, e);
            throw new BlinkServiceException("IO error during DELETE request to " + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted during DELETE request to {}", path, e);
            throw new BlinkServiceException("Interrupted during DELETE request to " + path, e);
        }
    }

    /**
     * Execute a GET request that returns a List.
     */
    public <R> java.util.List<R> getList(String path, Class<R> elementType) throws BlinkServiceException {
        return getList(path, elementType, UUID.randomUUID().toString());
    }

    /**
     * Execute a GET request that returns a List with custom request ID.
     */
    public <R> java.util.List<R> getList(String path, Class<R> elementType, String requestId)
            throws BlinkServiceException {
        try {
            String accessToken = tokenManager.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(config.getTimeout())
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("request-id", requestId)
                    .GET()
                    .build();

            log.debug("GET {} (list) with request-id: {}", path, requestId);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleListResponse(response, elementType);

        } catch (IOException e) {
            log.error("IO error during GET request to {}", path, e);
            throw new BlinkServiceException("IO error during GET request to " + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted during GET request to {}", path, e);
            throw new BlinkServiceException("Interrupted during GET request to " + path, e);
        }
    }

    private <R> R handleResponse(HttpResponse<String> response, Class<R> responseType)
            throws BlinkServiceException {
        try {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (response.body() == null || response.body().isEmpty()) {
                    return null;
                }
                return objectMapper.readValue(response.body(), responseType);
            } else {
                String errorMessage = String.format("Request failed: HTTP %d - %s",
                        response.statusCode(), response.body());
                log.error(errorMessage);
                throw new BlinkServiceException(errorMessage);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize response body", e);
            throw new BlinkServiceException("Failed to deserialize response body", e);
        }
    }

    private <R> java.util.List<R> handleListResponse(HttpResponse<String> response, Class<R> elementType)
            throws BlinkServiceException {
        try {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (response.body() == null || response.body().isEmpty()) {
                    return java.util.Collections.emptyList();
                }
                return objectMapper.readValue(response.body(),
                        objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, elementType));
            } else {
                String errorMessage = String.format("Request failed: HTTP %d - %s",
                        response.statusCode(), response.body());
                log.error(errorMessage);
                throw new BlinkServiceException(errorMessage);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize response body", e);
            throw new BlinkServiceException("Failed to deserialize response body", e);
        }
    }
}
