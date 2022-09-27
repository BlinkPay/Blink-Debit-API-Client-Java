# Blink Debit API Client for Java
![badge](https://github.com/BlinkPay/Blink-Debit-API-Client-Java/actions/workflows/workflow.yml/badge.svg)

This SDK allows merchants with Java-based e-commerce site to integrate with Blink PayNow and Blink AutoPay.

# Minimum Requirements
- Maven 3
- Java 8

This SDK uses WebClient, a reactive web client introduced in Spring Framework 5, for making API calls.

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
- Customise/supply the required properties in your `application.yaml` or `application.properties`. Sandbox debit URL is `https://sandbox.debit.blinkpay.co.nz` and production debit URL is `https://debit.blinkpay.co.nz`. The client credentials will be provided to you as part of the on-boarding process.
```yaml
blinkpay:
  debit:
    url: <BLINKPAY_DEBIT_URL>
  max:
    connections: 10
    idle:
      time: PT20S
    life:
      time: PT60S
  pending:
    acquire:
      timeout: PT10S
  eviction:
    interval: PT60S
  client:
    id: <BLINKPAY_CLIENT_ID>
    secret: <BLINKPAY_CLIENT_SECRET>
```
```properties
blinkpay.debit.url=<BLINKPAY_DEBIT_URL>
blinkpay.max.connections=10
blinkpay.max.idle.time=PT20S
blinkpay.max.life.time=PT60S
blinkpay.pending.acquire.timeout=PT10S
blinkpay.eviction.interval=PT60S
blinkpay.client.id=<BLINKPAY_CLIENT_ID>
blinkpay.client.secret=<BLINKPAY_CLIENT_SECRET>
```

# Integration
WebClient builder and access token handler must be injected when instantiating the API clients. Alternatively, Spring-based client code can simply autowire the API clients. Optional correlation ID can be added as the last argument.
## Access Token Handler
```java
private OAuthApiClient oauthApiClient = new OAuthApiClient(webClientBuilder, clientId, clientSecret);
private AccessTokenHandler accessTokenHandler = new AccessTokenHandler(oauthApiClient);
```

## Bank Metadata
```java
private MetaApiClient client = new MetaApiClient(webClientBuilder, accessTokenHandler);
```
```java
Flux<BankMetadata> bankMetadataFlux = client.getMeta();
```

## Single Consents
```java
private SingleConsentsApiClient client = new SingleConsentsApiClient(webClientBuilder, accessTokenHandler);
```
### Redirect Flow
```java
Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(AuthFlowDetail.TypeEnum.REDIRECT,
        bank, redirectUri, particulars, code, reference, total);
```
### Decoupled Flow
```java
Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(AuthFlowDetail.TypeEnum.DECOUPLED,
        bank, null, particulars, code, reference, total, null, identifierType, identifierValue, callbackUrl);
```
### Gateway Flow - Redirect Flow Hint
```java
Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(AuthFlowDetail.TypeEnum.GATEWAY,
        bank, redirectUri, particulars, code, reference, total, FlowHint.TypeEnum.REDIRECT, null, null, null);
```
### Gateway Flow - Decoupled Flow Hint
```java
Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsent(AuthFlowDetail.TypeEnum.GATEWAY,
        bank, redirectUri, particulars, code, reference, total, FlowHint.TypeEnum.DECOUPLED, identifierType,
        identifierValue, null);
```
### Retrieval
```java
Mono<Consent> consentMono = client.getSingleConsent(consentId);
```
### Revocation
```java
Mono<Void> voidMono = client.revokeSingleConsent(consentId);
```

## Enduring Consents
```java
private EnduringConsentsApiClient client = new EnduringConsentsApiClient(webClientBuilder, accessTokenHandler);
```
### Redirect Flow
```java
Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(AuthFlowDetail.TypeEnum.REDIRECT,
        bank, redirectUri, period, startDate, endDate, maximumAmount);
```
### Decoupled Flow
```java
Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(AuthFlowDetail.TypeEnum.DECOUPLED,
        bank, null, period, startDate, endDate, maximumAmount, null, identifierType,identifierValue, callbackUrl);
```
### Gateway Flow - Redirect Flow Hint
```java
Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY,
        bank, redirectUri, period, startDate, endDate, maximumAmount, FlowHint.TypeEnum.REDIRECT, null, null, null);
```
### Gateway Flow - Decoupled Flow Hint
```java
Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(AuthFlowDetail.TypeEnum.GATEWAY,
        bank, redirectUri, period, startDate, endDate, maximumAmount, FlowHint.TypeEnum.DECOUPLED, identifierType,
        identifierValue, null);
```
### Retrieval
```java
Mono<Consent> consentMono = client.getEnduringConsent(consentId);
```
### Revocation
```java
Mono<Void> voidMono = client.revokeEnduringConsent(consentId);
```

## Payments
```java
private PaymentsApiClient client = new PaymentsApiClient(webClientBuilder, accessTokenHandler);
```
### Single Consent
```java
Mono<PaymentResponse> paymentResponseMono = client.createPayment(consentId);
```
### Enduring Consent
```java
Mono<PaymentResponse> paymentResponseMono = client.createPayment(consentId, particulars, code, reference, total);
```
### Westpac
```java
Mono<PaymentResponse> paymentResponseMono = client.createPayment(consentId, accountReferenceId);
```
### Retrieval
```java
Mono<Payment> paymentMono = client.getPayment(paymentId);
```

## Quick Payments
```java
private QuickPaymentsApiClient client = new QuickPaymentsApiClient((webClientBuilder, accessTokenHandler);
```
### Redirect Flow
```java
Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono =
        client.createQuickPayment(AuthFlowDetail.TypeEnum.REDIRECT, bank, redirectUri, particulars, code, reference,
        total, null);
```
### Decoupled Flow
```java
Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono =
        client.createQuickPayment(AuthFlowDetail.TypeEnum.DECOUPLED, bank, null, particulars, code, reference, total,
        null, identifierType, identifierValue, callbackUrl);
```
### Gateway Flow - Redirect Flow Hint
```java
Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono =
        client.createQuickPayment(AuthFlowDetail.TypeEnum.GATEWAY, bank, redirectUri, particulars, code, reference,
        total, FlowHint.TypeEnum.REDIRECT);
```
### Gateway Flow - Decoupled Flow Hint
```java
Mono<CreateQuickPaymentResponse> createQuickPaymentResponseResponseMono =
        client.createQuickPayment(AuthFlowDetail.TypeEnum.GATEWAY, bank, redirectUri, particulars, code, reference,
        total, FlowHint.TypeEnum.DECOUPLED, identifierType, identifierValue, null);
```
### Retrieval
```java
Mono<QuickPaymentResponse> quickPaymentResponseMono = client.getQuickPayment(quickPaymentId);
```
### Revocation
```java
Mono<Void> voidMono = client.revokeQuickPayment(quickPaymentId);
```

## Refunds
```java
private RefundsApiClient client = new RefundsApiClient(webClientBuilder, accessTokenHandler);
```
### Account Number Refund
```java
Mono<RefundResponse> refundResponseMono = client.createRefund(RefundDetail.TypeEnum.ACCOUNT_NUMBER, paymentId);
```
### Full Refund
```java
Mono<RefundResponse> refundResponseMono = client.createRefund(RefundDetail.TypeEnum.FULL_REFUND, paymentId,
        redirectUri, particulars, code, reference);
```
### Partial Refund (Not yet implemented)
```java
Mono<RefundResponse> refundResponseMono = client.createRefund(RefundDetail.TypeEnum.PARTIAL_REFUND, paymentId,
        redirectUri, particulars, code, reference, total, correlationId);
```
### Retrieval
```java
Mono<Refund> refundMono = client.getRefund(refundId);
```