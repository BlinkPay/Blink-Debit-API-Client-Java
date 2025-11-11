# Blink Debit API Client for Java
[![CI](https://github.com/BlinkPay/Blink-Debit-API-Client-Java/actions/workflows/maven-build.yml/badge.svg)](https://github.com/BlinkPay/Blink-Debit-API-Client-Java/actions/workflows/maven-build.yml)

---
## blink-debit-api-client-java
[![Maven Central](https://img.shields.io/maven-central/v/nz.co.blinkpay/blink-debit-api-client-java.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/nz.co.blinkpay/blink-debit-api-client-java)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=blink-debit-api-client-java&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=blink-debit-api-client-java)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=blink-debit-api-client-java&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=blink-debit-api-client-java)
[![Snyk security](https://snyk.io/test/github/BlinkPay/Blink-Debit-API-Client-Java/badge.svg)](https://security.snyk.io/package/maven/nz.co.blinkpay:blink-debit-api-client-java)

---
## blink-debit-api-client-java-spring6
[![Maven Central](https://img.shields.io/maven-central/v/nz.co.blinkpay/blink-debit-api-client-java-spring6.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/nz.co.blinkpay/blink-debit-api-client-java-spring6)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=blink-debit-api-client-java-spring6&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=blink-debit-api-client-java-spring6)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=blink-debit-api-client-java-spring6&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=blink-debit-api-client-java-spring6)
[![Snyk security](https://snyk.io/test/github/BlinkPay/Blink-Debit-API-Client-Java/badge.svg)](https://security.snyk.io/package/maven/nz.co.blinkpay:blink-debit-api-client-java-spring6)

# Table of Contents
1. [Introduction](#introduction)
2. [Contributing](#contributing)
3. [Minimum Requirements](#minimum-requirements)
4. [Dependency](#adding-the-dependency)
5. [Quick Start](#quick-start)
6. [Configuration](#configuration)
7. [Client Creation](#client-creation)
8. [Correlation ID](#correlation-id)
9. [Full Examples](#full-examples)
10. [Individual API Call Examples](#individual-api-call-examples)

## Introduction
This SDK allows merchants with Java-based e-commerce site to integrate with **Blink PayNow** (for one-off payments) and **Blink AutoPay** (for recurring payments).

### SDK Versions
This repository provides two SDK implementations:

- **v1 SDK** (`java/` and `java-spring6/` modules): Reactive SDK using Spring WebClient with Mono/Flux async programming. Suitable for Spring-based applications.
- **v2 SDK** (`java-v2/` module - **Recommended**): Lightweight synchronous SDK using Java 11+ HttpClient. **87% smaller** dependency footprint (~206KB vs ~1.6MB runtime dependencies), ideal for plain Java applications, serverless functions, and memory-constrained environments.

The v1 SDK internally uses WebClient, a reactive Web client introduced in Spring Framework 5, for making API calls. The v2 SDK uses the standard Java 11+ HttpClient for synchronous blocking calls with minimal dependencies.

## Contributing
We welcome contributions from the community. Your pull request will be reviewed by our team.

This project is licensed under the MIT License.

### Running Tests

The project includes unit and integration tests. To run tests, you need to set the required environment variables:

```bash
export BLINKPAY_CLIENT_ID="your-client-id"
export BLINKPAY_CLIENT_SECRET="your-client-secret"
```

Then run the tests:

```bash
# Run unit tests only
mvn -B -ntp -Dgroups=unit test

# Run integration tests only
mvn -B -ntp -Dgroups=integration test

# Run all tests
mvn -B -ntp test
```

Or combine the environment variables in a single command:

```bash
# Unit tests
BLINKPAY_CLIENT_ID="your-client-id" BLINKPAY_CLIENT_SECRET="your-client-secret" mvn -B -ntp -Dgroups=unit test

# All tests
BLINKPAY_CLIENT_ID="your-client-id" BLINKPAY_CLIENT_SECRET="your-client-secret" mvn -B -ntp test
```

For the Spring 6 module, navigate to the `java-spring6` directory first:

```bash
cd java-spring6
BLINKPAY_CLIENT_ID="your-client-id" BLINKPAY_CLIENT_SECRET="your-client-secret" mvn -B -ntp -Dgroups=unit test
```

## Minimum Requirements
- Maven 3 or Gradle 7
- Java 11 or higher (for `blink-debit-api-client-java` and `blink-debit-api-client-java-v2`)
- Java 21 or higher (for `blink-debit-api-client-java-spring6`)
- Lombok 1.18 (for v1 SDK development only - not required for v2 SDK)

## Adding the dependency

This SDK is available in three versions to support different Java and Spring Framework configurations:

### For Plain Java 11+ Applications (Recommended - v2 SDK)
Use `blink-debit-api-client-java-v2` for:
- **Plain Java 11+** applications (non-Spring)
- **Serverless functions** (Lambda, Cloud Functions, etc.)
- **Memory-constrained environments**
- Applications that prefer **synchronous blocking API** calls

This lightweight version has **87% fewer runtime dependencies** compared to v1 SDK.

#### Maven
```xml
<dependency>
    <groupId>nz.co.blinkpay</groupId>
    <artifactId>blink-debit-api-client-java-v2</artifactId>
    <version>${version}</version>
</dependency>
```

#### Gradle
```groovy
implementation "nz.co.blinkpay:blink-debit-api-client-java-v2:$version"
```

### For Spring Boot 2.x Applications (v1 SDK)
Use `blink-debit-api-client-java` for:
- **Spring Boot 2.x** applications
- **Spring Framework versions < 6**
- Applications that prefer **reactive programming** with Mono/Flux

#### Maven
```xml
<dependency>
    <groupId>nz.co.blinkpay</groupId>
    <artifactId>blink-debit-api-client-java</artifactId>
    <version>${version}</version>
</dependency>
```

#### Gradle
```groovy
implementation "nz.co.blinkpay:blink-debit-api-client-java:$version"
```

### For Spring Boot 3.x / Spring 6
Use `blink-debit-api-client-java-spring6` **only if** you are using:
- **Spring Framework 6+** (including Spring Boot 3.x)

#### Maven
```xml
<dependency>
    <groupId>nz.co.blinkpay</groupId>
    <artifactId>blink-debit-api-client-java-spring6</artifactId>
    <version>${version}</version>
</dependency>
```

#### Gradle
```groovy
implementation "nz.co.blinkpay:blink-debit-api-client-java-spring6:$version"
```

## Quick Start

### v2 SDK (Recommended for Plain Java)
```java
import nz.co.blink.debit.client.v1.BlinkDebitClient;
import nz.co.blink.debit.config.BlinkDebitConfig;

// Option 1: Using environment variables
BlinkDebitClient client = new BlinkDebitClient();

// Option 2: Using configuration builder
BlinkDebitConfig config = BlinkDebitConfig.builder()
        .debitUrl("https://sandbox.debit.blinkpay.co.nz")
        .clientId("your-client-id")
        .clientSecret("your-client-secret")
        .build();
BlinkDebitClient client = new BlinkDebitClient(config);

// Create a quick payment (synchronous blocking call)
QuickPaymentRequest request = new QuickPaymentRequest()
        .flow(new AuthFlow()
                .detail(new GatewayFlow()
                        .redirectUri("https://www.example.com/return")))
        .amount(new Amount()
                .currency(Amount.CurrencyEnum.NZD)
                .total("10.00"))
        .pcr(new Pcr()
                .particulars("particulars")
                .code("code")
                .reference("reference"));

CreateQuickPaymentResponse response = client.getQuickPaymentsApi().createQuickPayment(request);
// Redirect the consumer to response.getRedirectUri()
```

### v1 SDK (Reactive WebClient)
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
            .total(totalPerPeriod))
        .maximumAmountPayment(new Amount()
                .currency(Amount.CurrencyEnum.NZD)
                .total(totalPerPayment))
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
            .total(totalPerPeriod))
        .maximumAmountPayment(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(totalPerPayment))
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
            .total(totalPerPeriod))
        .maximumAmountPayment(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(totalPerPayment))
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
            .total(totalPerPeriod))
        .maximumAmountPayment(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(totalPerPayment))
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
            .total(totalPerPeriod))
        .maximumAmountPayment(new Amount()
            .currency(Amount.CurrencyEnum.NZD)
            .total(totalPerPayment))
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