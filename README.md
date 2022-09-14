# Blink Debit API Client for Java
![badge](https://github.com/BlinkPay/Blink-Debit-API-Client-Java/actions/workflows/workflow.yml/badge.svg)

This SDK allows merchants with Java-based e-commerce site to integrate with Blink PayNow and Blink AutoPay.

# Minimum Requirements

- Java 8
- Spring Boot 2.7
- Maven 3.8.1

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
- Set the required environment variables:
`export BLINKPAY_CLIENT_ID=<YOUR_CLIENT_ID>;BLINKPAY_CLIENT_SECRET=<YOUR_CLIENT_SECRET>;BLINKPAY_BFF_SHARED_SECRET=<BFF_SHARED_SECRET>`
- Add the following properties to your `application.yaml`.
```yaml
blinkpay:
  debit:
    url: https://sandbox.debit.blinkpay.co.nz
  max:
    connections: 50
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
    id: ${BLINKPAY_CLIENT_ID}
    secret: ${BLINKPAY_CLIENT_SECRET}
  bff:
    shared:
      secret: ${BLINKPAY_BFF_SHARED_SECRET}
```

# Integration

### Authorisation
```java

```

### Bank Metadata
```java

```

### Single Consents
```java

```

### Enduring Consents
```java

```

### Gateway
```java

```

### Payments
```java

```

### Quick Payments
```java

```

### Refunds
```java

```