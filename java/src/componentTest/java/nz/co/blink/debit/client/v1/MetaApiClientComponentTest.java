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

import nz.co.blink.debit.config.BlinkDebitConfiguration;
import nz.co.blink.debit.config.BlinkPayProperties;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.BankMetadata;
import nz.co.blink.debit.dto.v1.BankmetadataFeatures;
import nz.co.blink.debit.dto.v1.BankmetadataFeaturesCardPayment;
import nz.co.blink.debit.dto.v1.BankmetadataFeaturesDecoupledFlow;
import nz.co.blink.debit.dto.v1.BankmetadataFeaturesDecoupledFlowAvailableIdentifiers;
import nz.co.blink.debit.dto.v1.BankmetadataFeaturesEnduringConsent;
import nz.co.blink.debit.dto.v1.BankmetadataRedirectFlow;
import nz.co.blink.debit.dto.v1.CardNetwork;
import nz.co.blink.debit.dto.v1.CardPaymentType;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The component test case for {@link MetaApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {AccessTokenHandler.class, OAuthApiClient.class, MetaApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
class MetaApiClientComponentTest {

    @Autowired
    private ReactorClientHttpConnector connector;

    @Autowired
    private BlinkPayProperties properties;

    private MetaApiClient client;

    @BeforeEach
    void setUp() {
        // use real host to generate valid access token
        BlinkPayProperties blinkPayProperties = new BlinkPayProperties();
        blinkPayProperties.getDebit().setUrl("https://sandbox.debit.blinkpay.co.nz");
        blinkPayProperties.getClient().setId("mock-client-id");
        blinkPayProperties.getClient().setSecret("mock-client-secret");
        OAuthApiClient oauthApiClient = new OAuthApiClient(connector, blinkPayProperties);

        client = new MetaApiClient(connector, properties, new AccessTokenHandler(oauthApiClient));
    }

    @Test
    @DisplayName("Verify that bank metadata is retrieved")
    void getMeta() throws BlinkServiceException {
        BankMetadata bnz = new BankMetadata()
                .name(Bank.BNZ)
                .paymentLimit(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50000"))
                .features(new BankmetadataFeatures()
                        .decoupledFlow(new BankmetadataFeaturesDecoupledFlow()
                                .enabled(true)
                                .availableIdentifiers(Collections.singletonList(
                                        new BankmetadataFeaturesDecoupledFlowAvailableIdentifiers()
                                                .type(IdentifierType.CONSENT_ID)
                                                .name("Consent ID")))
                                .requestTimeout("PT4M"))
                        .enduringConsent(new BankmetadataFeaturesEnduringConsent()
                                .enabled(true)
                                .consentIndefinite(false)))
                .redirectFlow(new BankmetadataRedirectFlow()
                        .enabled(true)
                        .requestTimeout("PT5M"));

        BankMetadata pnz = new BankMetadata()
                .name(Bank.PNZ)
                .paymentLimit(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("50000"))
                .features(new BankmetadataFeatures()
                        .enduringConsent(new BankmetadataFeaturesEnduringConsent()
                                .enabled(true)
                                .consentIndefinite(true))
                        .decoupledFlow(new BankmetadataFeaturesDecoupledFlow()
                                .enabled(true)
                                .availableIdentifiers(Stream.of(
                                                new BankmetadataFeaturesDecoupledFlowAvailableIdentifiers()
                                                        .type(IdentifierType.PHONE_NUMBER)
                                                        .name("Phone Number"),
                                                new BankmetadataFeaturesDecoupledFlowAvailableIdentifiers()
                                                        .type(IdentifierType.MOBILE_NUMBER)
                                                        .name("Mobile Number"))
                                        .collect(Collectors.toList()))
                                .requestTimeout("PT3M")))
                .redirectFlow(new BankmetadataRedirectFlow()
                        .enabled(true)
                        .requestTimeout("PT10M"));

        BankMetadata westpac = new BankMetadata()
                .name(Bank.WESTPAC)
                .paymentLimit(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("10000"))
                .features(new BankmetadataFeatures())
                .redirectFlow(new BankmetadataRedirectFlow()
                        .enabled(true)
                        .requestTimeout("PT10M"));

        BankMetadata asb = new BankMetadata()
                .name(Bank.ASB)
                .paymentLimit(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("30000"))
                .features(new BankmetadataFeatures()
                        .enduringConsent(new BankmetadataFeaturesEnduringConsent()
                                .enabled(true)
                                .consentIndefinite(false)))
                .redirectFlow(new BankmetadataRedirectFlow()
                        .enabled(true)
                        .requestTimeout("PT10M"));

        BankMetadata anz = new BankMetadata()
                .name(Bank.ANZ)
                .paymentLimit(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("1000"))
                .features(new BankmetadataFeatures()
                        .decoupledFlow(new BankmetadataFeaturesDecoupledFlow()
                                .enabled(true)
                                .availableIdentifiers(Stream.of(
                                                new BankmetadataFeaturesDecoupledFlowAvailableIdentifiers()
                                                        .type(IdentifierType.MOBILE_NUMBER)
                                                        .name("Mobile Number"))
                                        .collect(Collectors.toList()))
                                .requestTimeout("PT7M")))
                .redirectFlow(new BankmetadataRedirectFlow()
                        .enabled(true)
                        .requestTimeout("PT10M"));

        BankMetadata cybersource = new BankMetadata()
                .name(Bank.CYBERSOURCE)
                .features(new BankmetadataFeatures()
                        .cardPayment(new BankmetadataFeaturesCardPayment()
                                .enabled(true)
                                .allowedCardPaymentTypes(Stream.of(CardPaymentType.PANENTRY, CardPaymentType.GOOGLEPAY)
                                        .collect(Collectors.toList()))
                                .allowedCardNetworks(Stream.of(CardNetwork.VISA, CardNetwork.MASTERCARD,
                                                CardNetwork.AMEX)
                                        .collect(Collectors.toList()))));

        Flux<BankMetadata> actual = client.getMeta();

        assertThat(actual).isNotNull();
        Set<BankMetadata> set = new HashSet<>();
        StepVerifier
                .create(actual)
                .consumeNextWith(set::add)
                .consumeNextWith(set::add)
                .consumeNextWith(set::add)
                .consumeNextWith(set::add)
                .consumeNextWith(set::add)
                .consumeNextWith(set::add)
                .verifyComplete();
        assertThat(set)
                .hasSize(6)
                .containsExactlyInAnyOrder(bnz, pnz, westpac, asb, anz, cybersource);
    }
}
