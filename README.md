# Blink Debit API Client for Java
![badge](https://github.com/BlinkPay/Blink-Debit-API-Client-Java/actions/workflows/workflow.yml/badge.svg)

This SDK allows merchants with Java-based e-commerce site to integrate with Blink PayNow and Blink AutoPay.

# Minimum Requirements
- Maven 3 or Gradle 7
- Java 8
- Lombok 1.18

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
ReactorClientHttpConnector and AccessTokenHandler must be injected when instantiating the API clients. Alternatively, Spring-based client code can simply autowire the API clients. Optional correlation ID can be added as the last argument.
## Access Token Handler
```java
private OAuthApiClient oauthApiClient = new OAuthApiClient(reactorClientHttpConnector, debitUrl, clientId, clientSecret);
private AccessTokenHandler accessTokenHandler = new AccessTokenHandler(oauthApiClient);
```

## Bank Metadata
```java
private MetaApiClient client = new MetaApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
```
```java
Flux<BankMetadata> bankMetadataFlux = client.getMeta();
```

## Single/One-Off Consents
```java
private SingleConsentsApiClient client = new SingleConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
```
### Redirect Flow
```java
SingleConsentRequest request = new SingleConsentRequest()
        .flow(new AuthFlow()
            .detail(new RedirectFlow()
                .bank(bank)
                .redirectUri(redirectUri)))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsentWithRedirectFlow(request);
```
### Decoupled Flow
```java
SingleConsentRequest request = new SingleConsentRequest()
        .flow(new AuthFlow()
            .detail(new DecoupledFlow()
                .bank(bank)
                .identifierType(identifierType)
                .identifierValue(identifierValue)
                .callbackUrl(callbackUrl)))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsentWithDecoupledFlow(request);
```
### Gateway Flow - Redirect Flow Hint
```java
SingleConsentRequest request = new SingleConsentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)
                .flowHint(new RedirectFlowHint()
                    .bank(bank))))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsentWithGatewayFlow(request);
```
### Gateway Flow - Decoupled Flow Hint
```java
SingleConsentRequest request = new SingleConsentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)
                .flowHint(new DecoupledFlowHint()
                    .identifierType(identifierType)
                    .identifierValue(identifierValue)
                    .bank(bank))))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

Mono<CreateConsentResponse> createConsentResponseMono = client.createSingleConsentWithGatewayFlow(request);
```
### Retrieval
```java
Mono<Consent> consentMono = client.getSingleConsent(consentId);
```
### Revocation
```java
Mono<Void> voidMono = client.revokeSingleConsent(consentId);
```

## Enduring/Recurring Consents
```java
private EnduringConsentsApiClient client = new EnduringConsentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
```
### Redirect Flow
```java
EnduringConsentRequest request = new EnduringConsentRequest()
        .flow(new AuthFlow()
            .detail(new RedirectFlow()
                .bank(bank)
                .redirectUri(redirectUri)))
        .maximumAmountPeriod(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .period(period)
        .fromTimestamp(startDate)
        .expiryTimestamp(endDate);

Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsentWithRedirectFlow(request);
```
### Decoupled Flow
```java
EnduringConsentRequest request = new EnduringConsentRequest()
        .flow(new AuthFlow()
            .detail(new DecoupledFlow()
                .bank(bank)
                .identifierType(identifierType)
                .identifierValue(identifierValue)
                .callbackUrl(callbackUrl)))
        .maximumAmountPeriod(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .period(period)
        .fromTimestamp(startDate)
        .expiryTimestamp(endDate);

Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsentWithDecoupledFlow(request);
```
### Gateway Flow - Redirect Flow Hint
```java
EnduringConsentRequest request = new EnduringConsentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)
                .flowHint(new RedirectFlowHint()
                    .bank(bank))))
        .maximumAmountPeriod(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .period(period)
        .fromTimestamp(startDate)
        .expiryTimestamp(endDate);

Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsentWithGatewayFlow(request);
```
### Gateway Flow - Decoupled Flow Hint
```java
EnduringConsentRequest request = new EnduringConsentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)
                .flowHint(new DecoupledFlowHint()
                    .identifierType(identifierType)
                    .identifierValue(identifierValue)
                    .bank(bank))))
        .maximumAmountPeriod(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .period(period)
        .fromTimestamp(startDate)
        .expiryTimestamp(endDate);

Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsentWithGatewayFlow(request);
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
private PaymentsApiClient client = new PaymentsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
```
### Single/One-Off
```java
PaymentRequest request = new PaymentRequest()
        .consentId(consentId);

Mono<PaymentResponse> paymentResponseMono = client.createSinglePayment(request);
```
### Enduring/Recurring
```java
PaymentRequest request = new PaymentRequest()
        .consentId(consentId)
        .enduringPayment(new EnduringPaymentRequest()
            .amount(new Amount()
                .currency(Amount.CurrencyEnum.NZD)
                .total(total))
            .pcr(new Pcr()
                .particulars(particulars)
                .code(code)
                .reference(reference)));

Mono<PaymentResponse> paymentResponseMono = client.createEnduringPayment(request);
```
### Westpac
```java
PaymentRequest request = new PaymentRequest()
        .consentId(consentId)
        .accountReferenceId(accountReferenceId);

Mono<PaymentResponse> paymentResponseMono = client.createWestpacPayment(request);
```
### Retrieval
```java
Mono<Payment> paymentMono = client.getPayment(paymentId);
```

## Quick Payments
```java
private QuickPaymentsApiClient client = new QuickPaymentsApiClient((reactorClientHttpConnector, debitUrl, accessTokenHandler);
```
### Redirect Flow
```java
QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
        .flow(new AuthFlow()
            .detail(new RedirectFlow()
                .bank(bank)
                .redirectUri(redirectUri)))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

Mono<CreateQuickPaymentResponse> createQuickPaymentResponseMono = client.createQuickPaymentWithRedirectFlow(request);
```
### Decoupled Flow
```java
QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
        .flow(new AuthFlow()
            .detail(new RedirectFlow()
                .bank(bank)
                .identifierType(identifierType)
                .identifierValue(identifierValue)
                .callbackUrl(callbackUrl)))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

Mono<CreateQuickPaymentResponse> createQuickPaymentResponseMono = client.createQuickPaymentWithDecoupledFlow(request);
```
### Gateway Flow - Redirect Flow Hint
```java
QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)
                .flowHint(new RedirectFlowHint()
                    .bank(bank))))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

Mono<CreateQuickPaymentResponse> createQuickPaymentResponseMono = client.createQuickPaymentWithGatewayFlow(request);
```
### Gateway Flow - Decoupled Flow Hint
```java
QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)
                .flowHint(new DecoupledFlowHint()
                    .identifierType(identifierType)
                    .identifierValue(identifierValue)
                    .bank(bank))))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

Mono<CreateQuickPaymentResponse> createQuickPaymentResponseMono = client.createQuickPaymentWithGatewayFlow(request);
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
private RefundsApiClient client = new RefundsApiClient(reactorClientHttpConnector, debitUrl, accessTokenHandler);
```
### Account Number Refund
```java
AccountNumberRefundRequest request = (AccountNumberRefundRequest) new AccountNumberRefundRequest()
        .paymentId(paymentId);

Mono<RefundResponse> refundResponseMono = client.createAccountNumberRefund(request);
```
### Full Refund
```java
FullRefundRequest request = (FullRefundRequest) new FullRefundRequest()
        .consentRedirect(redirectUri)
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference))
        .paymentId(paymentId);

Mono<RefundResponse> refundResponseMono = client.createFullRefund(request);
```
### Partial Refund (Not yet implemented)
```java
PartialRefundRequest request = (PartialRefundRequest) new PartialRefundRequest()
        .consentRedirect(redirectUri)
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .paymentId(paymentId);

Mono<RefundResponse> refundResponseMono = client.createPartialRefund(request);
```
### Retrieval
```java
Mono<Refund> refundMono = client.getRefund(refundId);
```