# Blink Debit API Client v2 - Technical Documentation

Technical guide for developers and AI assistants working with the v2 SDK codebase.

## Architecture Overview

The v2 SDK is a lightweight, synchronous Java client for the Blink Debit API. It uses Java 11+ HttpClient for HTTP operations and has minimal runtime dependencies (~206KB).

### Design Principles

1. **Minimal Dependencies**: Only Jackson, java-jwt, and slf4j-api
2. **Synchronous Blocking API**: Simple request-response pattern
3. **Thread-Safe**: All components safe for concurrent use
4. **Immutable Configuration**: Builder pattern with validation
5. **AutoCloseable**: Proper resource lifecycle management

## Project Structure

```
java-v2/
├── src/main/java/nz/co/blink/debit/
│   ├── client/v1/          # API client implementations
│   │   ├── BlinkDebitClient.java          # Main facade
│   │   ├── OAuthApiClient.java            # OAuth2 authentication
│   │   ├── SingleConsentsApiClient.java   # Single consent operations
│   │   ├── EnduringConsentsApiClient.java # Enduring consent operations
│   │   ├── QuickPaymentsApiClient.java    # Quick payment operations
│   │   ├── PaymentsApiClient.java         # Payment operations
│   │   ├── RefundsApiClient.java          # Refund operations
│   │   └── MetaApiClient.java             # Bank metadata operations
│   ├── config/             # Configuration
│   │   └── BlinkDebitConfig.java          # Immutable config with builder
│   ├── dto/v1/             # Data transfer objects (generated)
│   │   ├── ApiClient.java                 # Base API client class
│   │   ├── JSON.java                      # JSON serialization utilities
│   │   ├── AbstractOpenApiSchema.java     # Base for polymorphic DTOs
│   │   ├── AccessTokenRequest.java        # OAuth token request
│   │   ├── AccessTokenResponse.java       # OAuth token response
│   │   └── [48 generated DTOs]
│   ├── enums/              # Enumeration types
│   │   ├── Bank.java
│   │   ├── ConsentStatus.java
│   │   ├── PaymentStatus.java
│   │   └── [other enums]
│   ├── exception/          # Exception classes
│   │   ├── BlinkServiceException.java     # API/HTTP errors
│   │   └── BlinkInvalidValueException.java # Validation errors
│   ├── helpers/            # Helper utilities
│   │   └── HttpClientHelper.java          # HTTP operations
│   └── service/            # Business logic
│       └── AccessTokenManager.java        # Token lifecycle management
└── src/integrationTest/java/
    └── nz/co/blink/debit/client/v1/
        └── BlinkDebitClientIntegrationTest.java
```

## Core Components

### 1. BlinkDebitClient (Main Facade)

**Location**: `src/main/java/nz/co/blink/debit/client/v1/BlinkDebitClient.java`

Entry point for all API operations. Implements `AutoCloseable`.

**Responsibilities**:
- Creates and manages HttpClient instance
- Initializes ObjectMapper with JavaTimeModule
- Creates AccessTokenManager for OAuth2
- Instantiates all API clients
- Provides getter methods for API access

**Usage**:
```java
try (BlinkDebitClient client = new BlinkDebitClient(config)) {
    client.getSingleConsentsApi().createSingleConsent(request);
}
```

**Thread Safety**: Thread-safe for read operations. HttpClient and ObjectMapper are thread-safe.

### 2. BlinkDebitConfig (Configuration)

**Location**: `src/main/java/nz/co/blink/debit/config/BlinkDebitConfig.java`

Immutable configuration object with builder pattern.

**Fields**:
- `debitUrl` (required): Base API URL
- `clientId` (required): OAuth2 client ID
- `clientSecret` (required): OAuth2 client secret
- `timeout` (optional): Request timeout, default 10 seconds

**Environment Variables**:
- `BLINKPAY_DEBIT_URL`
- `BLINKPAY_CLIENT_ID`
- `BLINKPAY_CLIENT_SECRET`
- `BLINKPAY_TIMEOUT` (ISO-8601 duration, e.g., "PT30S")

**Validation**: Constructor validates all required fields are present.

### 3. AccessTokenManager (OAuth2 Token Management)

**Location**: `src/main/java/nz/co/blink/debit/service/AccessTokenManager.java`

Thread-safe OAuth2 access token manager with automatic refresh.

**Features**:
- Lazy initialization (token fetched on first use)
- Automatic refresh 60 seconds before expiry
- Thread-safe using double-checked locking
- Uses Auth0-JWT to decode token expiry

**Algorithm**:
```java
public String getAccessToken() {
    if (token expired or not exists) {
        synchronized {
            if (token expired or not exists) {  // Double-check
                fetch new token
                decode expiry using JWT
            }
        }
    }
    return token
}
```

### 4. OAuthApiClient (OAuth2 Client)

**Location**: `src/main/java/nz/co/blink/debit/client/v1/OAuthApiClient.java`

Implements OAuth2 client credentials flow.

**Method**:
```java
public AccessTokenResponse generateAccessToken()
```

**Grant Type**: `client_credentials`

**Endpoint**: `POST /oauth2/token`

### 5. HttpClientHelper (HTTP Operations)

**Location**: `src/main/java/nz/co/blink/debit/helpers/HttpClientHelper.java`

Centralized HTTP client for all API calls.

**Methods**:
- `post(path, body, responseType, requestId)`: POST with JSON body
- `get(path, responseType, requestId)`: GET request
- `delete(path, requestId)`: DELETE request
- `getList(path, elementType, requestId)`: GET returning List

**Features**:
- Automatic Bearer token injection
- JSON serialization/deserialization via Jackson
- Request ID header (`request-id`)
- Comprehensive error handling
- Configurable timeout from BlinkDebitConfig

**Error Handling**:
- HTTP 2xx: Success, deserialize response
- HTTP 4xx/5xx: Throw BlinkServiceException with status code and body
- IOException: Wrap in BlinkServiceException
- InterruptedException: Set thread interrupted flag, wrap in BlinkServiceException

### 6. API Clients Pattern

All API clients follow this pattern:

**Structure**:
```java
public class XxxApiClient {
    private static final String PATH = "/path";
    private final HttpClientHelper httpHelper;

    public XxxApiClient(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    // Method with auto-generated request ID
    public Response method(Request request) {
        return method(request, UUID.randomUUID().toString());
    }

    // Method with custom request ID
    public Response method(Request request, String requestId) {
        validateInput(request);
        return httpHelper.post(PATH, request, Response.class, requestId);
    }
}
```

**Validation**: Use `BlinkInvalidValueException` for null/invalid inputs.

**API Clients**:
- **SingleConsentsApiClient**: `/consents/single` - create, get, revoke
- **EnduringConsentsApiClient**: `/consents/enduring` - create, get, revoke
- **QuickPaymentsApiClient**: `/quick-payments` - create, get, revoke
- **PaymentsApiClient**: `/payments` - create, get
- **RefundsApiClient**: `/refunds` - create, get
- **MetaApiClient**: `/meta` - get bank metadata (returns List)

## Data Transfer Objects (DTOs)

### Generation

DTOs are generated from OpenAPI specification using openapi-generator.

**Important**: Minimize manual edits to DTOs as they can be regenerated.

**Generator Configuration**:
- Tool: openapi-generator-maven-plugin
- Generator: java
- Library: native (no external HTTP client)
- Package: nz.co.blink.debit.dto.v1

### Manual Modifications

Only these DTOs should be manually edited:

#### 1. Polymorphic Parent Classes

Three parent classes require `TypeEnum` discriminator:

**AuthFlowDetail.java**:
```java
public abstract class AuthFlowDetail {
    public enum TypeEnum {
        GATEWAY, REDIRECT, DECOUPLED
    }
    public abstract TypeEnum getType();
    // ... rest of class
}
```

**ConsentDetail.java**:
```java
public abstract class ConsentDetail {
    public enum TypeEnum {
        SINGLE, ENDURING
    }
    public abstract TypeEnum getType();
    // ... rest of class
}
```

**RefundRequest.java**:
```java
public abstract class RefundRequest {
    public enum TypeEnum {
        PARTIAL_REFUND, FULL_REFUND
    }
    public abstract TypeEnum getType();
    // ... rest of class
}
```

Child classes implement `getType()` to return their discriminator value.

#### 2. OAuth DTOs

**AccessTokenRequest.java** and **AccessTokenResponse.java** are custom POJOs (not generated):
- Manual builder pattern
- No Lombok
- Standard getters/setters/equals/hashCode

#### 3. Import Additions

Some DTOs need `import nz.co.blink.debit.dto.ApiClient;` added for compilation.

### Base Classes

**ApiClient.java**: Base class providing shared utilities for generated DTOs.

**JSON.java**: Jackson-based JSON serialization/deserialization utilities.

**AbstractOpenApiSchema.java**: Base class for polymorphic DTOs. Has abstract `getSchemas()` method implemented by children.

## Exception Handling

### BlinkServiceException

**Location**: `src/main/java/nz/co/blink/debit/exception/BlinkServiceException.java`

Thrown for API call failures:
- HTTP 4xx/5xx responses
- Network/IO errors
- JSON serialization/deserialization errors

**Contains**: Error message with HTTP status code and response body when available.

### BlinkInvalidValueException

**Location**: `src/main/java/nz/co/blink/debit/exception/BlinkInvalidValueException.java`

Thrown for invalid input parameters:
- Null required parameters
- Invalid UUIDs
- Constraint violations

**Usage**: Validate inputs at API client entry points before making HTTP calls.

## Testing

### Integration Tests

**Location**: `src/integrationTest/java/nz/co/blink/debit/client/v1/BlinkDebitClientIntegrationTest.java`

**Configuration**:
- Uses JUnit 5 with `@Tag("integration")`
- Requires environment variables: `BLINKPAY_CLIENT_ID`, `BLINKPAY_CLIENT_SECRET`, `BLINKPAY_DEBIT_URL`
- Uses `assumeTrue()` to skip if credentials unavailable

**Maven Configuration**:
- `maven-surefire-plugin` excludes integration tests by default (`<excludedGroups>integration</excludedGroups>`)
- Run with: `mvn test -DexcludedGroups=""`

**Test Cases**:
1. `testGetMetadata()`: Verifies Meta API returns bank data
2. `testClientFromEnvironment()`: Tests environment variable initialization
3. `testGetApiClients()`: Validates all API client getters return non-null

### Unit Tests

Currently no unit tests. Future additions should:
- Mock HttpClient responses
- Test error handling paths
- Validate request construction
- Test token refresh logic

## Build and Package

### Maven Commands

```bash
# Compile
mvn clean compile

# Run tests (excludes integration by default)
mvn test

# Run with integration tests
mvn test -DexcludedGroups=""

# Package JAR
mvn package

# Skip tests
mvn package -DskipTests

# Generate dependency tree
mvn dependency:tree
```

### Build Output

- Main JAR: `target/blink-debit-api-client-java-v2-2.0.0-SNAPSHOT.jar` (~206 KB)
- Sources JAR: `target/blink-debit-api-client-java-v2-2.0.0-SNAPSHOT-sources.jar`
- Javadoc JAR: `target/blink-debit-api-client-java-v2-2.0.0-SNAPSHOT-javadoc.jar`

### Dependencies (Runtime)

**Compile Scope**:
- slf4j-api (2.0.17) - Logging facade
- jackson-core (2.20.1) - JSON processing
- jackson-databind (2.20.1) - JSON object mapping
- jackson-datatype-jsr310 (2.20.1) - Java 8 date/time support
- java-jwt (4.5.0) - JWT token decoding

**Optional**:
- validation-api (2.0.1.Final) - Bean validation annotations
- javax.annotation-api (1.3.2) - @Generated and @Nonnull annotations

**Test Scope**:
- junit-jupiter (6.0.1)
- assertj-core (3.27.6)
- spring-boot-starter-test (2.7.18) - Test infrastructure only

## Development Guidelines

### Adding a New API Client

1. Create class in `nz.co.blink.debit.client.v1` package
2. Follow existing API client pattern (see above)
3. Add field to `BlinkDebitClient`
4. Initialize in `BlinkDebitClient` constructor
5. Add getter method to `BlinkDebitClient`
6. Create integration test if applicable

### Modifying DTOs

**DO**:
- Regenerate from OpenAPI spec when API changes
- Add TypeEnum to polymorphic parents after regeneration
- Add required imports

**DON'T**:
- Modify generated DTO logic
- Add custom fields to generated DTOs
- Change serialization annotations

### Error Handling Pattern

```java
public Response apiMethod(Request request, String requestId) throws BlinkServiceException {
    // Validate inputs
    if (request == null) {
        throw new BlinkInvalidValueException("Request must not be null");
    }
    if (request.getId() == null) {
        throw new BlinkInvalidValueException("ID must not be null");
    }

    // Make HTTP call (may throw BlinkServiceException)
    return httpHelper.post(PATH, request, Response.class, requestId);
}
```

### Thread Safety

**Thread-Safe Components**:
- BlinkDebitClient (read operations)
- HttpClient (built-in thread safety)
- ObjectMapper (thread-safe after configuration)
- AccessTokenManager (synchronized token refresh)
- All API clients (stateless)

**Not Thread-Safe**:
- BlinkDebitConfig.Builder (don't share builder across threads)

### Logging

The SDK uses SLF4J for logging. Consumers must provide a logging implementation (e.g., Logback, Log4j2).

**Log Levels**:
- **INFO**: Client initialization, high-level operations
- **DEBUG**: HTTP request/response details, token refresh events
- **ERROR**: API failures, HTTP errors, exceptions

**Example Logback Configuration**:
```xml
<configuration>
    <logger name="nz.co.blink.debit" level="DEBUG"/>
</configuration>
```

## Common Patterns

### Request ID Handling

Every API method has two overloads:
```java
public Response method(Request request);  // Auto-generates UUID
public Response method(Request request, String requestId);  // Custom ID
```

Use custom request IDs for distributed tracing.

### Resource Cleanup

Always use try-with-resources:
```java
try (BlinkDebitClient client = new BlinkDebitClient(config)) {
    // Use client
} // Automatically closed
```

Or manual cleanup:
```java
BlinkDebitClient client = new BlinkDebitClient(config);
try {
    // Use client
} finally {
    client.close();
}
```

### Configuration from Environment

```java
// Uses BLINKPAY_* environment variables
BlinkDebitClient client = new BlinkDebitClient();
```

This calls `BlinkDebitConfig.fromEnvironment().build()` internally.

## API Endpoint Reference

| Operation | Client | Method | Path |
|-----------|--------|--------|------|
| Generate token | OAuthApiClient | generateAccessToken() | POST /oauth2/token |
| Create single consent | SingleConsentsApiClient | createSingleConsent() | POST /consents/single |
| Get consent | SingleConsentsApiClient | getConsent() | GET /consents/single/{id} |
| Revoke consent | SingleConsentsApiClient | revokeConsent() | DELETE /consents/single/{id} |
| Create enduring consent | EnduringConsentsApiClient | createEnduringConsent() | POST /consents/enduring |
| Get enduring consent | EnduringConsentsApiClient | getConsent() | GET /consents/enduring/{id} |
| Revoke enduring consent | EnduringConsentsApiClient | revokeConsent() | DELETE /consents/enduring/{id} |
| Create quick payment | QuickPaymentsApiClient | createQuickPayment() | POST /quick-payments |
| Get quick payment | QuickPaymentsApiClient | getQuickPayment() | GET /quick-payments/{id} |
| Revoke quick payment | QuickPaymentsApiClient | revokeQuickPayment() | DELETE /quick-payments/{id} |
| Create payment | PaymentsApiClient | createPayment() | POST /payments |
| Get payment | PaymentsApiClient | getPayment() | GET /payments/{id} |
| Create refund | RefundsApiClient | createRefund() | POST /refunds |
| Get refund | RefundsApiClient | getRefund() | GET /refunds/{id} |
| Get bank metadata | MetaApiClient | getMeta() | GET /meta |

## Notes for AI Assistants

- **DTOs**: Generated code - minimize manual edits
- **Polymorphism**: TypeEnum pattern for discriminated unions
- **Thread Safety**: All components thread-safe except Config.Builder
- **Error Handling**: BlinkServiceException for API, BlinkInvalidValueException for validation
- **Resource Management**: Always use try-with-resources or close() in finally
- **Testing**: Integration tests require real credentials, use @Tag("integration")
- **Commit Messages**: Use conventional commit format (feat:, fix:, docs:, etc.)
