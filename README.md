# Blink Debit API Client for Java
![badge](https://github.com/BlinkPay/Blink-Debit-API-Client-Java/actions/workflows/workflow.yml/badge.svg?branch=main)
[![blink-debit-api-client-java](https://img.shields.io/maven-central/v/nz.co.blinkpay/blink-debit-api-client-java.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22nz.co.blinkpay%22%20AND%20a:%22blink-debit-api-client-java%22)
[![blink-debit-api-client-java-spring6](https://img.shields.io/maven-central/v/nz.co.blinkpay/blink-debit-api-client-java-spring6.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22nz.co.blinkpay%22%20AND%20a:%22blink-debit-api-client-java-spring6%22)
[![Snyk](https://snyk.io/test/github/BlinkPay/Blink-Debit-API-Client-Java/badge.svg)](https://snyk.io/test/github/BlinkPay/Blink-Debit-API-Client-Java)

# Table of Contents
1. [Minimum Requirements](#minimum-requirements)
2. [Dependency](#adding-the-dependency)
3. [Quick Start](#quick-start)
4. [Configuration](#configuration)
5. [Client Creation](#client-creation)
6. [Correlation ID](#correlation-id)
7. [Full Examples](#full-examples)
8. [Individual API Call Examples](#individual-api-call-examples)

This SDK allows merchants with Java-based e-commerce site to integrate with Blink PayNow and Blink AutoPay.

This SDK internally uses WebClient, a reactive web client introduced in Spring Framework 5, for making API calls.

## Minimum Requirements
- Maven 3 or Gradle 7
- Java 8 or higher
- Lombok 1.18 (for development only)

## Adding the dependency
For: 
- Java 8 or higher using plan java 
- or with Spring versions less than 6 (i.e. including Spring Boot 2)
use `blink-debit-api-client-java`
### Maven
```xml
<dependency>
    <groupId>nz.co.blinkpay</groupId>
    <artifactId>blink-debit-api-client-java</artifactId>
    <version>1.0.0</version>
</dependency>
```
For Spring 6 (i.e. including Spring Boot 3), use `blink-debit-api-client-java-spring6`
```xml
<dependency>
    <groupId>nz.co.blinkpay</groupId>
    <artifactId>blink-debit-api-client-java-spring6</artifactId>
    <version>1.0.0</version>
</dependency>
```
### Gradle
```groovy
implementation 'nz.co.blinkpay:blink-debit-api-client-java:1.0.0
```
```groovy
implementation 'nz.co.blinkpay:blink-debit-api-client-java-spring6:1.0.0
```

## Quick Start
```java
String blinkpayUrl = "https://sandbox.debit.blinkpay.co.nz";
String clientId = "...";
String clientSecret = "...";
String profile = "local";
BlinkDebitClient client = new BlinkDebitClient(blinkpayUrl, clientId, clientSecret, profile);

QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
        .flow(new AuthFlow()
                .detail(new GatewayFlow()
                        .redirectUri("https://www.blinkpay.co.nz/sample-merchant-return-page")))
        .amount(new Amount()
                .currency(Amount.CurrencyEnum.NZD)
                .total("0.01"))
        .pcr(new Pcr()
                .particulars("particulars")
                .code("code")
                .reference("reference"));

CreateQuickPaymentResponse qpCreateResponse = client.createQuickPayment(request);
logger.info("Redirect URL: {}", qpCreateResponse.getRedirectUri()); // Redirect the consumer to this URL
UUID qpId = qpCreateResponse.getQuickPaymentId();
QuickPaymentResponse qpResponse = client.awaitSuccessfulQuickPaymentOrThrowException(qpId, 300); // Will throw an exception if the payment was not successful after 5min
```

## Configuration
- Customise/supply the required properties in your `blinkdebit.yaml` or `blinkdebit.properties`. This file should be available in your classpath, i.e. normally placed in `src/main/resources`.
- The BlinkPay **Sandbox** debit URL is `https://sandbox.debit.blinkpay.co.nz` and the **production** debit URL is `https://debit.blinkpay.co.nz`.
- The client credentials will be provided to you by BlinkPay as part of your on-boarding process. 
- Properties can be supplied using environment variables.
> **Warning** Take care not to check in your client ID and secret to your source control.

### Property precedence
Properties will be detected and loaded according to the heirarcy -
1. As provided directly to client constructor
2. Environment variables e.g. `export BLINKPAY_CLIENT_SECRET=...`
3. System properties e.g. `-Dblinkpay.client.secret=...`
4. `blinkdebit.properties`
5. `blinkdebit.yaml`
6. Default values

### Property set-up examples

#### Environment variables
```shell
export BLINKPAY_DEBIT_URL=<BLINKPAY_DEBIT_URL>
export BLINKPAY_CLIENT_ID=<BLINKPAY_CLIENT_ID>
export BLINKPAY_CLIENT_SECRET=<BLINKPAY_CLIENT_SECRET>
# for non-Spring consumer as an alternative to spring.profiles.active property. Debugging profiles are local, dev or test. Any other value will behave in a production-like manner.
export BLINKPAY_ACTIVE_PROFILE=test

# Optional configuration values below
export BLINKPAY_MAX_CONNECTIONS=10
export BLINKPAY_MAX_IDLE_TIME=PT20S
export BLINKPAY_MAX_LIFE_TIME=PT60S
export BLINKPAY_PENDING_ACQUIRE_timeout=PT10S
export BLINKPAY_EVICTION_INTERVAL=PT60S
export BLINKPAY_RETRY_ENABLED=true
```

### Properties file 
Substitute the correct values to your `blinkdebit.properties` file.
```properties
blinkpay.debit.url=<BLINKPAY_DEBIT_URL>
blinkpay.client.id=<BLINKPAY_CLIENT_ID>
blinkpay.client.secret=<BLINKPAY_CLIENT_SECRET>
# for non-Spring consumer as an alternative to spring.profiles.active property. Debugging profiles are local, dev or test. Any other value will behave in a production-like manner.
blinkpay.active.profile=test

# Optional configuration values below
blinkpay.max.connections=10
blinkpay.max.idle.time=PT20S
blinkpay.max.life.time=PT60S
blinkpay.pending.acquire.timeout=PT10S
blinkpay.eviction.interval=PT60S
blinkpay.retry.enabled=true
```

#### Properties file - Spring
The property placeholders below will **only work for Spring consumers** by substituting the corresponding environment variables.
```properties
blinkpay.debit.url=${BLINKPAY_DEBIT_URL}
blinkpay.client.id=${BLINKPAY_CLIENT_ID}
blinkpay.client.secret=${BLINKPAY_CLIENT_SECRET}
# for non-Spring consumer as an alternative to spring.profiles.active property. Debugging profiles are local, dev or test. Any other value will behave in a production-like manner.
blinkpay.active.profile=${BLINKPAY_ACTIVE_PROFILE:test}

# Optional configuration values below
blinkpay.max.connections=${BLINKPAY_MAX_CONNECTIONS:10}
blinkpay.max.idle.time=${BLINKPAY_MAX_IDLE_TIME:PT20S}
blinkpay.max.life.time=${BLINKPAY_MAX_LIFE_TIME:PT60S}
blinkpay.pending.acquire.timeout=${BLINKPAY_PENDING_ACQUIRE_TIMEOUT:PT10S}
blinkpay.eviction.interval=${BLINKPAY_EVICTION_INTERVAL:PT60S}
blinkpay.retry.enabled=${BLINKPAY_RETRY_ENABLED:true}
```

#### YAML properties file - Spring
The property placeholders below will **only work for Spring consumers** by substituting the corresponding environment variables.
```yaml
blinkpay:
  debit:
    url: ${BLINKPAY_DEBIT_URL}
  client:
    id: ${BLINKPAY_CLIENT_ID}
    secret: ${BLINKPAY_CLIENT_SECRET}
  
  # Optional configuration values below
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
  retry:
    enabled: ${BLINKPAY_RETRY_ENABLED:true}
```

## Client creation
### Java
Plain Java client code can use the no-arg constructor which will attempt to populate the properties according to the hierarchy above.

```java
BlinkDebitClient client = new BlinkDebitClient();
```

Another way is to supply the required properties on object creation:
```java
BlinkDebitClient client = new BlinkDebitClient(blinkpayUrl, clientId, clientSecret, "production");
```

### Spring
Spring-based client code can simply autowire/inject the API client when properties are supplied as above.
```java
@Autowired
BlinkDebitClient client;
```

## Correlation ID
An optional correlation ID can be added as the last argument to API calls. This is also the idempotency key for Blink API calls. 

It will be generated for you automatically if it is not provided.

## Full Examples
### Quick payment (one-off payment), using Gateway flow
A quick payment is a one-off payment that combines the API calls needed for both the consent and the payment.
```java
QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
        .flow(new AuthFlow()
                .detail(new GatewayFlow()
                        .redirectUri("https://www.blinkpay.co.nz/sample-merchant-return-page")))
        .amount(new Amount()
                .currency(Amount.CurrencyEnum.NZD)
                .total("0.01"))
        .pcr(new Pcr()
                .particulars("particulars")
                .code("code")
                .reference("reference"));

CreateQuickPaymentResponse qpCreateResponse = client.createQuickPayment(request);
logger.info("Redirect URL: {}", qpCreateResponse.getRedirectUri()); // Redirect the consumer to this URL
UUID qpId = qpCreateResponse.getQuickPaymentId();
QuickPaymentResponse qpResponse = client.awaitSuccessfulQuickPaymentOrThrowException(qpId, 300); // Will throw an exception if the payment was not successful after 5min
```

### Single consent followed by one-off payment, using Gateway flow
```java
SingleConsentRequest consent = new SingleConsentRequest()
        .flow(new AuthFlow()
                .detail(new GatewayFlow()
                        .redirectUri("https://www.blinkpay.co.nz/sample-merchant-return-page")
                        .flowHint(new RedirectFlowHint()
                                .bank(Bank.BNZ)))) // Optional, bank will be preselected
        .amount(new Amount()
                .currency(Amount.CurrencyEnum.NZD)
                .total("0.01")) 
        .pcr(new Pcr()
                .particulars("particulars"));
CreateConsentResponse createConsentResponse = client.createSingleConsent(consent);
String redirectUri = createConsentResponse.getRedirectUri(); // Redirect the consumer to this URL
PaymentRequest payment = new PaymentRequest().consentId(createConsentResponse.getConsentId());
PaymentResponse paymentResponse = client.createPayment(payment);
logger.info("Payment Status: {}", client.getPayment(paymentResponse.getPaymentId()).getStatus());
// TODO inspect the payment result status
```

## Individual API Call Examples
### Bank Metadata
Supplies the supported banks and supported flows on your account.
```java
List<BankMetadata> bankMetadataList = client.getMeta();
```

### Quick Payments
#### Gateway Flow
```java
QuickPaymentRequest request = (QuickPaymentRequest) new QuickPaymentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPayment(request);
```
#### Gateway Flow - Redirect Flow Hint
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

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPayment(request);
```
#### Gateway Flow - Decoupled Flow Hint
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

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPayment(request);
```
#### Redirect Flow
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

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPayment(request);
```
#### Decoupled Flow
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

CreateQuickPaymentResponse createQuickPaymentResponse = client.createQuickPayment(request);
```
#### Retrieval
```java
QuickPaymentResponse quickPaymentResponse = client.getQuickPayment(quickPaymentId);
```
#### Revocation
```java
client.revokeQuickPayment(quickPaymentId);
```

### Single/One-Off Consents
#### Gateway Flow
```java
SingleConsentRequest request = new SingleConsentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)))
        .amount(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference));

CreateConsentResponse createConsentResponse = client.createSingleConsent(request);
```
#### Gateway Flow - Redirect Flow Hint
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

CreateConsentResponse createConsentResponse = client.createSingleConsent(request);
```
#### Gateway Flow - Decoupled Flow Hint
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

CreateConsentResponse createConsentResponse = client.createSingleConsent(request);
```
#### Redirect Flow
Suitable for most consents.
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

CreateConsentResponse createConsentResponse = client.createSingleConsent(request);
```
#### Decoupled Flow
This flow type allows better support for mobile by allowing the supply of a mobile number or previous consent ID to identify the customer with their bank.

The customer will receive the consent request directly to their online banking app. This flow does not send the user through a web redirect flow.
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

CreateConsentResponse createConsentResponse = client.createSingleConsent(request);
```
#### Retrieval
Get the consent including its status
```java
Consent consent = client.getSingleConsent(consentId);
```
#### Revocation
```java
client.revokeSingleConsent(consentId);
```

### Blink AutoPay - Enduring/Recurring Consents
Request an ongoing authorisation from the customer to debit their account on a recurring basis.

Note that such an authorisation can be revoked by the customer in their mobile banking app.
#### Gateway Flow
```java
EnduringConsentRequest request = new EnduringConsentRequest()
        .flow(new AuthFlow()
            .detail(new GatewayFlow()
                .redirectUri(redirectUri)))
        .maximumAmountPeriod(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(total))
        .period(period)
        .fromTimestamp(startDate)
        .expiryTimestamp(endDate);

CreateConsentResponse createConsentResponse = client.createEnduringConsent(request);
```
#### Gateway Flow - Redirect Flow Hint
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

CreateConsentResponse createConsentResponse = client.createEnduringConsent(request);
```
#### Gateway Flow - Decoupled Flow Hint
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

CreateConsentResponse createConsentResponse = client.createEnduringConsent(request);
```
#### Redirect Flow
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

CreateConsentResponse createConsentResponse = client.createEnduringConsent(request);
```
#### Decoupled Flow
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

CreateConsentResponse createConsentResponse = client.createEnduringConsent(request);
```
#### Retrieval
```java
Consent consent = client.getEnduringConsent(consentId);
```
#### Revocation
```java
client.revokeEnduringConsent(consentId);
```

### Payments
The completion of a payment requires a consent to be in the Authorised status.
#### Single/One-Off
```java
PaymentRequest request = new PaymentRequest()
        .consentId(consentId);

PaymentResponse paymentResponse = client.createPayment(request);
```
#### Enduring/Recurring
If you already have an approved consent, you can run a Payment against that consent at the frequency as authorised in the consent.
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

PaymentResponse paymentResponse = client.createPayment(request);
```
#### Westpac
Westpac requires you to specify which account of the customers to debit. 

The available selection of accounts is supplied to you in the consent response of an Authorised Westpac consent object, and the ID of the selected account in supplied here.
```java
PaymentRequest request = new PaymentRequest()
        .consentId(consentId)
        .accountReferenceId(accountReferenceId);

PaymentResponse paymentResponse = client.createWestpacPayment(request);
```
#### Retrieval
```java
Payment payment = client.getPayment(paymentId);
```

### Refunds
#### Account Number Refund
```java
AccountNumberRefundRequest request = (AccountNumberRefundRequest) new AccountNumberRefundRequest()
        .paymentId(paymentId);

RefundResponse refundResponse = client.createRefund(request);
```
#### Full Refund (Not yet implemented)
```java
FullRefundRequest request = (FullRefundRequest) new FullRefundRequest()
        .consentRedirect(redirectUri)
        .pcr(new Pcr()
            .particulars(particulars)
            .code(code)
            .reference(reference))
        .paymentId(paymentId);

RefundResponse refundResponse = client.createRefund(request);
```
#### Partial Refund (Not yet implemented)
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

RefundResponse refundResponse = client.createRefund(request);
```
#### Retrieval
```java
Refund refund = client.getRefund(refundId);
```