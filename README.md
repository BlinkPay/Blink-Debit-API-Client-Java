# Blink Debit API Client for Java
![badge](https://github.com/BlinkPay/Blink-Debit-API-Client-Java/actions/workflows/workflow.yml/badge.svg)

This SDK allows merchants with Java-based e-commerce site to integrate with Blink PayNow and Blink AutoPay.

# Minimum Requirements
- Java 8
- Spring Boot 2
- Maven 3

# Dependency
## Maven
```xml
<dependency>
    <groupId>nz.co.blinkpay</groupId>
    <artifactId>blink-debit-api-client-java</artifactId>
    <version>1.0.0</version>
</dependency>
```
## Gradle
```
implementation 'nz.co.blinkpay:blink-debit-api-client-java:1.0.0
```

# Configuration
- Customise/supply the required properties in your `application.yaml`. Sandbox debit URL is `https://sandbox.debit.blinkpay.co.nz` and production debit URL is `https://debit.blinkpay.co.nz`.
```yaml
blinkpay:
  debit:
    url: ${BLINKPAY_DEBIT_URL:https://sandbox.debit.blinkpay.co.nz}
  max:
    connections: ${BLINKPAY_MAX_CONNECTIONS:10}
    idle:
      time: ${BLINKPAY_MAX_IDLE_TIME:PT20S}
    life:
      time: ${BLINKPAY_MAX_LIFE_TIME:PT60S}
  pending:
    acquire:
      timeout: ${BLINKPAY_PENDING_ACQUIRE_TIMEOUT:PT10S}
  eviction:
    interval: ${BLINKPAY_EVICTION_INTERVAL:PT60S}
  client:
    id: ${BLINKPAY_CLIENT_ID:}
    secret: ${BLINKPAY_CLIENT_SECRET:}
```

# Integration

## Authorisation
Request for an access token once a day or when the current one is about to expire and store in memory to be used throughout the client code.
```java
AccessTokenResponse accessTokenResponse = client.generateAccessToken(correlationId).block();
String accessToken = accessTokenResponse.getAccessToken();
```

## Bank Metadata
```java
Flux<BankMetadata> bankMetadataFlux = client.getMeta(correlationId, accessToken);
```

## Single Consents
### Redirect Flow
```java
CreateConsentResponse createConsentResponse = client.createSingleConsent(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.REDIRECT, bank, redirectUri, particulars, code, reference, total).block();
```
### Decoupled Flow
```java
CreateConsentResponse createConsentResponse = client.createSingleConsent(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.DECOUPLED, bank, null, particulars, code, reference, total, null, identifierType,
        identifierValue, callbackUrl).block();
```
### Gateway Flow - Redirect Flow Hint
```java
CreateConsentResponse createConsentResponse = client.createSingleConsent(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.GATEWAY, bank, redirectUri, particulars, code, reference, total,
        FlowHint.TypeEnum.REDIRECT, null, null, null).block();
```
### Gateway Flow - Decoupled Flow Hint
```java
CreateConsentResponse createConsentResponse = client.createSingleConsent(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.GATEWAY, bank, redirectUri, particulars, code, reference, total,
        FlowHint.TypeEnum.DECOUPLED, identifierType, identifierValue, null).block();
```
### Retrieval
```java
Consent consent = client.getSingleConsent(correlationId, accessToken, consentId).block();
```
### Revocation
```java
client.revokeSingleConsent(correlationId, accessToken, consentId).block();
```

## Enduring Consents
### Redirect Flow
```java
CreateConsentResponse createConsentResponse = client.createEnduringConsent(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.REDIRECT, bank, redirectUri, period, startDate, endDate, maximumAmount).block();
```
### Decoupled Flow
```java
CreateConsentResponse createConsentResponse = client.createEnduringConsent(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.DECOUPLED, bank, null, period, startDate, endDate, maximumAmount, null, identifierType,
        identifierValue, callbackUrl).block();
```
### Gateway Flow - Redirect Flow Hint
```java
CreateConsentResponse createConsentResponse = client.createEnduringConsent(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.GATEWAY, bank, redirectUri, period, startDate, endDate, maximumAmount,
        FlowHint.TypeEnum.REDIRECT, null, null, null).block();
```
### Gateway Flow - Decoupled Flow Hint
```java
CreateConsentResponse createConsentResponse = client.createEnduringConsent(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.GATEWAY, bank, redirectUri, period, startDate, endDate, maximumAmount,
        FlowHint.TypeEnum.DECOUPLED, identifierType, identifierValue, null).block();
```
### Retrieval
```java
Consent consent = client.getEnduringConsent(correlationId, accessToken, consentId).block();
```
### Revocation
```java
client.revokeEnduringConsent(correlationId, accessToken, consentId).block();
```

## Payments
### Single Consent
```java
PaymentResponse paymentResponse = client.createPayment(correlationId, accessToken, consentId).block();
```
### Enduring Consent
```java
PaymentResponse paymentResponse = client.createPayment(correlationId, accessToken, consentId, null, particulars, code,
        reference, total).block();
```
### Westpac
```java
PaymentResponse paymentResponse = client.createPayment(correlationId, accessToken, consentId, accountReferenceId).block();
```
### Retrieval
```java
Payment payment = client.getPayment(correlationId, accessToken, paymentId).block();
```

## Quick Payments
### Redirect Flow
```java
CreateQuickPaymentResponse createQuickPaymentResponseResponse = client.createQuickPayment(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.REDIRECT, bank, redirectUri, particulars, code, reference, total, null).block();
```
### Decoupled Flow
```java
CreateQuickPaymentResponse createQuickPaymentResponseResponse = client.createQuickPayment(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.DECOUPLED, bank, null, particulars, code, reference, total, null, identifierType,
        identifierValue, callbackUrl).block();
```
### Gateway Flow - Redirect Flow Hint
```java
CreateQuickPaymentResponse createQuickPaymentResponseResponse = client.createQuickPayment(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.GATEWAY, bank, redirectUri, particulars, code, reference, total,
        FlowHint.TypeEnum.REDIRECT).block();
```
### Gateway Flow - Decoupled Flow Hint
```java
CreateQuickPaymentResponse createQuickPaymentResponseResponse = client.createQuickPayment(correlationId, accessToken,
        AuthFlowDetail.TypeEnum.GATEWAY, bank, redirectUri, particulars, code, reference, total,
        FlowHint.TypeEnum.DECOUPLED, identifierType, identifierValue, null).block();
```
### Retrieval
```java
QuickPaymentResponse quickPaymentResponse = client.getQuickPayment(correlationId, accessToken, quickPaymentId).block();
```
### Revocation
```java
client.revokeQuickPayment(correlationId, accessToken, quickPaymentId).block();
```

## Refunds
### Account Number Refund
```java
RefundResponse refundResponse = client.createRefund(correlationId, accessToken, RefundDetail.TypeEnum.ACCOUNT_NUMBER,
        paymentId).block();
```
### Full Refund
```java
RefundResponse refundResponse = client.createRefund(correlationId, accessToken, RefundDetail.TypeEnum.FULL_REFUND,
        paymentId, redirectUri, particulars, code, reference, total).block();
```
### Partial Refund (Not yet implemented)
```java
RefundResponse refundResponse = client.createRefund(correlationId, accessToken, RefundDetail.TypeEnum.PARTIAL_REFUND,
        paymentId, redirectUri, particulars, code, reference, total).block();
```
### Retrieval
```java
Refund refund = client.getRefund(correlationId, accessToken, refundId).block();
```