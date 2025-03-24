/**
 * Copyright (c) 2022 BlinkPay
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package nz.co.blink.debit.client.v1;

import io.github.resilience4j.retry.Retry;
import nz.co.blink.debit.config.BlinkDebitConfiguration;
import nz.co.blink.debit.config.BlinkPayProperties;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.AuthFlow;
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.Consent;
import nz.co.blink.debit.dto.v1.ConsentDetail;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.DecoupledFlow;
import nz.co.blink.debit.dto.v1.DecoupledFlowHint;
import nz.co.blink.debit.dto.v1.EnduringConsentRequest;
import nz.co.blink.debit.dto.v1.GatewayFlow;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Period;
import nz.co.blink.debit.dto.v1.RedirectFlow;
import nz.co.blink.debit.dto.v1.RedirectFlowHint;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import nz.co.blink.debit.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * The component test case for {@link EnduringConsentsApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {AccessTokenHandler.class, OAuthApiClient.class, EnduringConsentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnduringConsentsApiClientComponentTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

    @Autowired
    private ReactorClientHttpConnector connector;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private Retry retry;

    @Autowired
    private BlinkPayProperties properties;

    private EnduringConsentsApiClient client;

    @BeforeEach
    void setUp() {
        // use real host to generate valid access token
        BlinkPayProperties blinkPayProperties = new BlinkPayProperties();
        blinkPayProperties.getDebit().setUrl("https://sandbox.debit.blinkpay.co.nz");
        blinkPayProperties.getClient().setId(System.getenv("BLINKPAY_CLIENT_ID"));
        blinkPayProperties.getClient().setSecret(System.getenv("BLINKPAY_CLIENT_SECRET"));
        OAuthApiClient oauthApiClient = new OAuthApiClient(connector, blinkPayProperties, retry);

        client = new EnduringConsentsApiClient(connector, properties, new AccessTokenHandler(oauthApiClient),
                validationService, retry);
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is created")
    @Order(1)
    void createEnduringConsentWithRedirectFlow() throws BlinkServiceException {
        EnduringConsentRequest request = new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(new RedirectFlow()
                                .bank(Bank.PNZ)
                                .redirectUri(REDIRECT_URI)
                                .redirectToApp(true)))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .period(Period.FORTNIGHTLY)
                .fromTimestamp(OffsetDateTime.now(ZONE_ID))
                .expiryTimestamp(OffsetDateTime.now(ZONE_ID).plusMonths(11))
                .hashedCustomerIdentifier("88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e");

        Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("8e916a6f-2a5d-4cb1-8b0b-8e8bb9677458"),
                        "https://obabank.glueware.dev/auth/login?oba_request=eyJhbGciOiJQUzUxMiIsInR5cCI6IkpXVCIsImtpZCI6ImZkYzAzZmJlLWExOTAtNDFlZS1iNDk2LWE1ODRmM2Y0YTZkMiJ9.eyJoeWJyaWRfcmVxX2lkIjoiYTU0NmE0MzMtOTk3OS00MGU5LWIwNjctY2VmNDQ0ZmMyMDY5LWRwY182NzE4NGFmMGRjMzM2ZjAwMjMzNzlkMDYiLCJpYXQiOjE3Mjk2NDUyOTcsImV4cCI6MTcyOTY0NzA5NywiaXNzIjoiaHR0cHM6Ly9hcGktbm9tYXRscy5hcGljZW50cmUubWlkZGxld2FyZS5jby5uei8iLCJqdGkiOiI3NGU3NDQ1Yi1hOWUzLTRkNmUtODE5Yy00OTVlYzU4MmNiMzYifQ.rVZ6JUVdAPz954WSgSNVyz9xQXnc6itbKqF3dTlAD0hKevBI1e10k3Af35XBqlszqjMlF6zXJzql8vhIZ0QiJ3OzWyr_vUTYneKJ03npo0-qWznVKluTQQnTPYz00pF8JRSuWfHvTpBtXM1vU_JBqfbPPDj0kWPzHl4ojw4hD4oKoA0uDcJHB9CgArcXsDUhI6lmQy0h7-tT2PmHUB8qJCBU-XoPnSpEMRwwazhQTeMu1_7BiM8RPi2PAibrZuZmffwZ419_Y5Etn--5ll8U0XvssY5Qq7wkkyY4M1qJeQe8mPztwoAexLTgmPgd_H8FzYoboRSfFcXo7saaybH-ysPObKq_l9xP26S1ES3uzJQsBuIs4jieUDYkDrPfhtIBCjctosMY7a1BNkM1WtLeu0jjWTJNxsgjcYPDSPxeEELweI0yYHmE-LUegSb5oqrtZTKtfIs6DONKQ52tIO1qudZJ14-HoBHiUZhx_3CGWn31ZRX-LS8HFEzfEe0RF9uo5vwamUv4OfOk9Q51cDJFNty-UhVy-vHEekYLviAqkz0IeDYprQmndEnWViumDooXGkHL_rWL_2yplUk8rjnQhWTbz8MboQ5kUARoe2R481bcTgkKvJOuROlUOiK89SdL5tZVHpCZzUwykpQgPNIdxjWOrXDEIQkzAXA08N4KwW0");
    }

    @Test
    @DisplayName("Verify that enduring consent with redirect flow is retrieved")
    @Order(2)
    void getEnduringConsentWithRedirectFlow() throws BlinkServiceException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.fromString("8e916a6f-2a5d-4cb1-8b0b-8e8bb9677458"));

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getPayments, Consent::getCardNetwork)
                .containsExactly(Consent.StatusEnum.AWAITINGAUTHORISATION, Collections.emptyList(), null);
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(RedirectFlow.class);
        RedirectFlow flow = (RedirectFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .extracting(RedirectFlow::getType, RedirectFlow::getBank, RedirectFlow::getRedirectUri)
                .containsExactly(AuthFlowDetail.TypeEnum.REDIRECT, Bank.PNZ, REDIRECT_URI);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNotNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent is revoked")
    @Order(3)
    void revokeEnduringConsent() {
        assertThatNoException().isThrownBy(() ->
                client.revokeEnduringConsent(UUID.fromString("0d48f138-2681-4af1-afeb-3351407b9daa")).block());
    }

    @Test
    @DisplayName("Verify that enduring consent with decoupled flow is created")
    @Order(4)
    void createEnduringConsentWithDecoupledFlow() throws BlinkServiceException {
        EnduringConsentRequest request = new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(new DecoupledFlow()
                                .bank(Bank.PNZ)
                                .identifierType(IdentifierType.PHONE_NUMBER)
                                .identifierValue("+64-259531933")
                                .callbackUrl("callbackUrl")))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .period(Period.FORTNIGHTLY)
                .fromTimestamp(OffsetDateTime.now(ZONE_ID))
                .expiryTimestamp(OffsetDateTime.now(ZONE_ID).plusMonths(11))
                .hashedCustomerIdentifier("88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e");

        Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId)
                .isEqualTo(UUID.fromString("294dda40-0357-4970-a86f-5b4974b880aa"));
    }

    @Test
    @DisplayName("Verify that enduring consent with decoupled flow is retrieved")
    @Order(5)
    void getEnduringConsentWithDecoupledFlow() throws BlinkServiceException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.fromString("294dda40-0357-4970-a86f-5b4974b880aa"));

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getPayments, Consent::getCardNetwork)
                .containsExactly(Consent.StatusEnum.AUTHORISED, Collections.emptyList(), null);
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(DecoupledFlow.class);
        DecoupledFlow flow = (DecoupledFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(DecoupledFlow::getType, DecoupledFlow::getBank, DecoupledFlow::getIdentifierType,
                        DecoupledFlow::getIdentifierValue)
                .containsExactly(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, IdentifierType.PHONE_NUMBER,
                        "+64-259531933");
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNotNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and redirect flow hint is created")
    @Order(6)
    void createEnduringConsentWithGatewayFlowAndRedirectFlowHint() throws BlinkServiceException {
        EnduringConsentRequest request = new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new RedirectFlowHint()
                                        .bank(Bank.PNZ))))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .period(Period.FORTNIGHTLY)
                .fromTimestamp(OffsetDateTime.now(ZONE_ID))
                .expiryTimestamp(OffsetDateTime.now(ZONE_ID).plusMonths(11))
                .hashedCustomerIdentifier("88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e");

        Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("44b7169f-90a0-4b8d-b723-056363a3fe53"),
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=44b7169f-90a0-4b8d-b723-056363a3fe53");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow and decoupled flow hint is created")
    @Order(7)
    void createEnduringConsentWithGatewayFlowAndDecoupledFlowHint() throws BlinkServiceException {
        EnduringConsentRequest request = new EnduringConsentRequest()
                .flow(new AuthFlow()
                        .detail(new GatewayFlow()
                                .redirectUri(REDIRECT_URI)
                                .flowHint(new DecoupledFlowHint()
                                        .identifierType(IdentifierType.PHONE_NUMBER)
                                        .identifierValue("+64-259531933")
                                        .bank(Bank.PNZ))))
                .maximumAmountPeriod(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50.00"))
                .period(Period.FORTNIGHTLY)
                .fromTimestamp(OffsetDateTime.now(ZONE_ID))
                .expiryTimestamp(OffsetDateTime.now(ZONE_ID).plusMonths(11))
                .hashedCustomerIdentifier("88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e");

        Mono<CreateConsentResponse> createConsentResponseMono = client.createEnduringConsent(request);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse actual = createConsentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(CreateConsentResponse::getConsentId, CreateConsentResponse::getRedirectUri)
                .containsExactly(UUID.fromString("44b7169f-90a0-4b8d-b723-056363a3fe53"),
                        "https://sandbox.secure.blinkpay.co.nz/gateway/pay?id=44b7169f-90a0-4b8d-b723-056363a3fe53");
    }

    @Test
    @DisplayName("Verify that enduring consent with gateway flow is retrieved")
    @Order(7)
    void getEnduringConsentWithGatewayFlow() throws BlinkServiceException {
        Mono<Consent> consentMono = client.getEnduringConsent(UUID.fromString("44b7169f-90a0-4b8d-b723-056363a3fe53"));

        assertThat(consentMono).isNotNull();
        Consent actual = consentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Consent::getStatus, Consent::getPayments, Consent::getCardNetwork)
                .containsExactly(Consent.StatusEnum.GATEWAYAWAITINGSUBMISSION, Collections.emptyList(), null);
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNull();
        assertThat(actual.getDetail())
                .isNotNull()
                .isInstanceOf(EnduringConsentRequest.class);
        EnduringConsentRequest detail = (EnduringConsentRequest) actual.getDetail();
        assertThat(detail.getType()).isEqualTo(ConsentDetail.TypeEnum.ENDURING);
        assertThat(detail.getFlow()).isNotNull();
        assertThat(detail.getFlow().getDetail())
                .isNotNull()
                .isInstanceOf(GatewayFlow.class);
        GatewayFlow flow = (GatewayFlow) detail.getFlow().getDetail();
        assertThat(flow)
                .isNotNull()
                .extracting(GatewayFlow::getType, GatewayFlow::getRedirectUri, GatewayFlow::getFlowHint)
                .containsExactly(AuthFlowDetail.TypeEnum.GATEWAY, REDIRECT_URI, null);
        assertThat(detail.getPeriod()).isEqualTo(Period.FORTNIGHTLY);
        assertThat(detail.getFromTimestamp()).isNotNull();
        assertThat(detail.getExpiryTimestamp()).isNotNull();
        assertThat(detail.getMaximumAmountPeriod())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "50.00");
    }
}
