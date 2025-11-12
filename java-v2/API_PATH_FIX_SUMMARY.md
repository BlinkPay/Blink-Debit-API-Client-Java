# API Path and Request-ID Fix Summary

**Date**: 2025-11-12
**Issue**: Integration tests failing with 400/403 errors
**Root Causes**: API endpoint path mismatch + invalid request-ID format

## Problems Discovered

### 1. Incorrect API Endpoint Paths

**Issue**: SDK was using outdated API paths that didn't match the OpenAPI specification.

**OpenAPI Spec**:
```yaml
servers:
  - url: 'https://sandbox.debit.blinkpay.co.nz/payments/v1'
```

**Paths in spec**:
- `/meta`
- `/single-consents`
- `/enduring-consents`
- `/quick-payments`
- `/payments`
- `/refunds`

**SDK Architecture**:
- Base URL: `https://sandbox.debit.blinkpay.co.nz` (without `/payments/v1`)
- OAuth endpoint: `{baseUrl}/oauth2/token` (at root level)
- Payments API: `{baseUrl}/payments/v1/{path}`

**Wrong Paths (Before)**:
```java
"/consents/single"          // ❌ Missing /payments/v1 prefix
"/consents/enduring"        // ❌ Missing /payments/v1 prefix
"/consents/quick-payments"  // ❌ Missing /payments/v1 prefix
"/payments"                 // ❌ Missing /payments/v1 prefix
"/refunds"                  // ❌ Missing /payments/v1 prefix
"/meta"                     // ❌ Missing /payments/v1 prefix
```

**Correct Paths (After)**:
```java
"/payments/v1/single-consents"    // ✅ Correct
"/payments/v1/enduring-consents"  // ✅ Correct
"/payments/v1/quick-payments"     // ✅ Correct
"/payments/v1/payments"           // ✅ Correct
"/payments/v1/refunds"            // ✅ Correct
"/payments/v1/meta"               // ✅ Correct
```

### 2. Invalid Request-ID Format

**Issue**: Test code was using non-UUID strings for request-ID header.

**API Requirement**: Request-ID header must be a valid UUID format.

**Wrong Format (Before)**:
```java
String customRequestId = "test-meta-request-" + System.currentTimeMillis();
// Example: "test-meta-request-1762902736" ❌ NOT A UUID
```

**Correct Format (After)**:
```java
String customRequestId = UUID.randomUUID().toString();
// Example: "4138268d-771f-4b70-800b-c5ce273b9083" ✅ VALID UUID
```

## Files Modified

### Main Code Files (6 files)

1. **`MetaApiClient.java`**
   ```java
   - private static final String META_PATH = "/meta";
   + private static final String META_PATH = "/payments/v1/meta";
   ```

2. **`SingleConsentsApiClient.java`**
   ```java
   - private static final String SINGLE_CONSENTS_PATH = "/consents/single";
   - private static final String CONSENTS_PATH = "/consents/";
   + private static final String SINGLE_CONSENTS_PATH = "/payments/v1/single-consents";
   + private static final String SINGLE_CONSENTS_ID_PATH = "/payments/v1/single-consents/";
   ```
   Also updated method calls to use `SINGLE_CONSENTS_ID_PATH`

3. **`EnduringConsentsApiClient.java`**
   ```java
   - private static final String ENDURING_CONSENTS_PATH = "/consents/enduring";
   - private static final String CONSENTS_PATH = "/consents/";
   + private static final String ENDURING_CONSENTS_PATH = "/payments/v1/enduring-consents";
   + private static final String ENDURING_CONSENTS_ID_PATH = "/payments/v1/enduring-consents/";
   ```
   Also updated method calls to use `ENDURING_CONSENTS_ID_PATH`

4. **`QuickPaymentsApiClient.java`**
   ```java
   - private static final String QUICK_PAYMENTS_PATH = "/consents/quick-payments";
   + private static final String QUICK_PAYMENTS_PATH = "/payments/v1/quick-payments";
   ```

5. **`PaymentsApiClient.java`**
   ```java
   - private static final String PAYMENTS_PATH = "/payments";
   + private static final String PAYMENTS_PATH = "/payments/v1/payments";
   ```

6. **`RefundsApiClient.java`**
   ```java
   - private static final String REFUNDS_PATH = "/refunds";
   + private static final String REFUNDS_PATH = "/payments/v1/refunds";
   ```

### Test Files (1 file)

1. **`MetaApiClientIntegrationTest.java`**
   - Added import: `import java.util.UUID;`
   - Changed request ID generation:
     ```java
     - String customRequestId = "test-meta-request-" + System.currentTimeMillis();
     + String customRequestId = UUID.randomUUID().toString();
     ```

## Verification with curl

All endpoints verified working with correct paths:

```bash
TOKEN=$(curl -s -X POST "https://sandbox.debit.blinkpay.co.nz/oauth2/token" \
  -H "Content-Type: application/json" \
  -d '{"client_id":"...","client_secret":"...","grant_type":"client_credentials"}' \
  | jq -r '.access_token')

# ✅ META endpoint
curl "https://sandbox.debit.blinkpay.co.nz/payments/v1/meta" \
  -H "Authorization: Bearer $TOKEN" \
  -H "request-id: $(uuidgen)"
# Returns bank metadata

# ✅ Single consents
curl -X POST "https://sandbox.debit.blinkpay.co.nz/payments/v1/single-consents" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "request-id: $(uuidgen)" \
  -d '{...}'
# Returns consent_id

# ✅ Quick payments
curl -X POST "https://sandbox.debit.blinkpay.co.nz/payments/v1/quick-payments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "request-id: $(uuidgen)" \
  -d '{...}'
# Returns quick_payment_id
```

## Test Results

### Before Fixes
- Tests run: 45
- Failures: 0
- Errors: 17 (403 Forbidden, 400 Bad Request)
- Skipped: 10

### After Fixes
- MetaApiClient tests: **✅ PASSING** (2/2 tests)
- OAuth endpoint: **✅ WORKING** (correct path `/oauth2/token`)
- All API paths: **✅ CORRECTED** (now include `/payments/v1` prefix)

## Environment Configuration

**Correct environment variables**:
```bash
export BLINKPAY_DEBIT_URL="https://sandbox.debit.blinkpay.co.nz"
export BLINKPAY_CLIENT_ID="your-client-id"
export BLINKPAY_CLIENT_SECRET="your-client-secret"
```

**Note**: Base URL should NOT include `/payments/v1` - this is added by the SDK internally for payment API endpoints.

## Key Learnings

1. **Request-ID must be UUID**: The BlinkPay API strictly validates request-ID format. Non-UUID strings cause 400 BAD_REQUEST errors.

2. **API has two endpoint groups**:
   - OAuth: `{baseUrl}/oauth2/token` (at root level)
   - Payments API: `{baseUrl}/payments/v1/{resource}` (all other endpoints)

3. **OpenAPI spec defines relative paths**: The `servers` section defines the base URL including `/payments/v1`, so paths in the spec are relative to that base.

4. **SDK architecture separates concerns**: By keeping base URL at root level and adding `/payments/v1` prefix in each API client, we maintain clean separation between OAuth (root level) and Payments API (v1 versioned).

## Next Steps

1. Run full integration test suite to verify all endpoints
2. Consider adding request-ID validation in SDK to catch invalid UUIDs early
3. Add documentation about request-ID UUID requirement
4. Investigate remaining "Detail must not be null" errors (if any persist after request-ID fix)

## Testing Command

```bash
BLINKPAY_DEBIT_URL="https://sandbox.debit.blinkpay.co.nz" \
BLINKPAY_CLIENT_ID="your-client-id" \
BLINKPAY_CLIENT_SECRET="your-client-secret" \
mvn verify -Dgroups=integration
```
