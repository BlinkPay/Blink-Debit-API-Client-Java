# Blink Debit API Client for Java
![badge](https://github.com/BlinkPay/Blink-Debit-API-Client-Java/actions/workflows/workflow.yml/badge.svg)

This SDK allows merchants with Java-based e-commerce site to integrate with Blink PayNow and Blink AutoPay.

# Minimum Requirements
- Maven 3 or Gradle 7
- Java 8
- Lombok 1.18

This SDK internally uses WebClient, a reactive web client introduced in Spring Framework 5, for making API calls.

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
- Customise/supply the required properties in your `application.yaml` or `application.properties`. This file should be available in your classpath, i.e. normally placed in `src/main/resources`.
- Sandbox debit URL is `https://sandbox.debit.blinkpay.co.nz` and production debit URL is `https://debit.blinkpay.co.nz`.
- The client credentials will be provided to you as part of the on-boarding process. These properties can also be supplied using environment variables.
> **Warning** Take care not to check in your client ID and secret to your source control.
```yaml
blinkpay:
  debit:
    url: ${BLINKPAY_DEBIT_URL}
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
    id: ${BLINKPAY_CLIENT_ID}
    secret: ${BLINKPAY_CLIENT_SECRET}
```
```properties
blinkpay.debit.url=${BLINKPAY_DEBIT_URL}
blinkpay.max.connections=${BLINKPAY_MAX_CONNECTIONS:10}
blinkpay.max.idle.time=${BLINKPAY_MAX_IDLE_TIME:PT20S}
blinkpay.max.life.time=${BLINKPAY_MAX_LIFE_TIME:PT60S}
blinkpay.pending.acquire.timeout=${BLINKPAY_PENDING_ACQUIRE_TIMEOUT:PT10S}
blinkpay.eviction.interval=${BLINKPAY_EVICTION_INTERVAL:PT60S}
blinkpay.client.id=${BLINKPAY_CLIENT_ID}
blinkpay.client.secret=${BLINKPAY_CLIENT_SECRET}
# for non-Spring consumer as an alternative to spring.profiles.active property
blinkpay.active.profile=${BLINKPAY_ACTIVE_PROFILE}
```

# Integration
## Client
Spring-based client code can simply autowire/inject the API client.
```java
@Autowired
BlinkDebitClient client;
```
Pure Java client code can load the contents of `application.properties` into Properties.
```java
Properties properties = new Properties();
properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));

BlinkDebitClient client = new BlinkDebitClient(properties);
```
Optional correlation ID can be added as the last argument to API calls.

## Bank Metadata
```java
List<BankMetadata> bankMetadataList = client.getMeta();
```

## Single/One-Off Consents
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

CreateConsentResponse createConsentResponse = client.createSingleConsentWithRedirectFlow(request);
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

CreateConsentResponse createConsentResponse = client.createSingleConsentWithDecoupledFlow(request);
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

CreateConsentResponse createConsentResponse = client.createSingleConsentWithGatewayFlow(request);
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

CreateConsentResponse createConsentResponse = client.createSingleConsentWithGatewayFlow(request);
```
### Retrieval
```java
Consent consent = client.getSingleConsent(consentId);
```
### Revocation
```java
client.revokeSingleConsent(consentId);
```

## Enduring/Recurring Consents
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

CreateConsentResponse createConsentResponse = client.createEnduringConsentWithRedirectFlow(request);
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

CreateConsentResponse createConsentResponse = client.createEnduringConsentWithDecoupledFlow(request);
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

CreateConsentResponse createConsentResponse = client.createEnduringConsentWithGatewayFlow(request);
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

CreateConsentResponse createConsentResponse = client.createEnduringConsentWithGatewayFlow(request);
```
### Retrieval
```java
Consent consent = client.getEnduringConsent(consentId);
```
### Revocation
```java
client.revokeEnduringConsent(consentId);
```

## Payments
### Single/One-Off
```java
PaymentRequest request = new PaymentRequest()
        .consentId(consentId);

PaymentResponse paymentResponse = client.createSinglePayment(request);
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

PaymentResponse paymentResponse = client.createEnduringPayment(request);
```
### Westpac
```java
PaymentRequest request = new PaymentRequest()
        .consentId(consentId)
        .accountReferenceId(accountReferenceId);

PaymentResponse paymentResponse = client.createWestpacPayment(request);
```
### Retrieval
```java
Payment payment = client.getPayment(paymentId);
```

## Quick Payments
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

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPaymentWithRedirectFlow(request);
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

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPaymentWithDecoupledFlow(request);
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

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPaymentWithGatewayFlow(request);
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

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPaymentWithGatewayFlow(request);
```
### Retrieval
```java
QuickPaymentResponse quickPaymentResponse = client.getQuickPayment(quickPaymentId);
```
### Revocation
```java
client.revokeQuickPayment(quickPaymentId);
```

## Refunds
### Account Number Refund
```java
AccountNumberRefundRequest request = (AccountNumberRefundRequest) new AccountNumberRefundRequest()
        .paymentId(paymentId);

RefundResponse refundResponse = client.createAccountNumberRefund(request);
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

RefundResponse refundResponse = client.createFullRefund(request);
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

RefundResponse refundResponse = client.createPartialRefund(request);
```
### Retrieval
```java
Refund refund = client.getRefund(refundId);
```