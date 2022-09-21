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
import nz.co.blink.debit.dto.v1.AccessTokenResponse;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.EnduringPaymentRequest;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.exception.ExpiredAccessTokenException;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The component test case for {@link PaymentsApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {OAuthApiClient.class, PaymentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentsApiClientComponentTest {

    @Autowired
    private OAuthApiClient oAuthApiClient;

    @Autowired
    private PaymentsApiClient client;

    private static String accessToken;

    @BeforeEach
    void setUp() {
        if (StringUtils.isBlank(accessToken)) {
            ReflectionTestUtils.setField(oAuthApiClient, "webClient", WebClient.create("https://staging.debit.blinkpay.co.nz"));

            Mono<AccessTokenResponse> accessTokenResponseMono = oAuthApiClient.generateAccessToken(UUID.randomUUID().toString());

            assertThat(accessTokenResponseMono).isNotNull();
            AccessTokenResponse accessTokenResponse = accessTokenResponseMono.block();
            assertThat(accessTokenResponse).isNotNull();

            accessToken = accessTokenResponse.getAccessToken();
            assertThat(accessToken)
                    .isNotBlank()
                    .startsWith("ey");
        }
    }

    @Test
    @DisplayName("Verify that payment for single consent is created")
    @Order(1)
    void createPaymentForSingleConsent() throws ExpiredAccessTokenException {
        Mono<PaymentResponse> paymentResponseMono = client.createPayment(UUID.randomUUID().toString(),
                accessToken, UUID.fromString("c14427fb-8ae8-4e5f-8685-3f6ab4c2f99a"));

        assertThat(paymentResponseMono).isNotNull();
        PaymentResponse actual = paymentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(PaymentResponse::getPaymentId)
                .isEqualTo(UUID.fromString("76ac9fa3-4793-45fe-8682-c7876fc5262e"));
    }

    @Test
    @DisplayName("Verify that payment for single consent is retrieved")
    @Order(2)
    void getPaymentForSingleConsent() throws ExpiredAccessTokenException {
        UUID paymentId = UUID.fromString("76ac9fa3-4793-45fe-8682-c7876fc5262e");
        Mono<Payment> paymentMono = client.getPayment(UUID.randomUUID().toString(), accessToken, paymentId);

        assertThat(paymentMono).isNotNull();
        Payment actual = paymentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Payment::getPaymentId, Payment::getType, Payment::getStatus, Payment::getRefunds)
                .containsExactly(paymentId, Payment.TypeEnum.SINGLE, Payment.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED,
                        Collections.emptyList());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        PaymentRequest paymentRequest = actual.getDetail();
        assertThat(paymentRequest)
                .isNotNull()
                .extracting(PaymentRequest::getConsentId, PaymentRequest::getAccountReferenceId,
                        PaymentRequest::getEnduringPayment)
                .containsExactly(UUID.fromString("c14427fb-8ae8-4e5f-8685-3f6ab4c2f99a"), null, null);
    }

    @Test
    @DisplayName("Verify that payment for enduring consent is created")
    @Order(3)
    void createPaymentForEnduringConsent() throws ExpiredAccessTokenException {
        Mono<PaymentResponse> paymentResponseMono = client.createPayment(UUID.randomUUID().toString(),
                accessToken, UUID.fromString("0500c560-c156-439f-9aed-753d82884323"), null, "particulars", "code",
                "reference", "45.00");

        assertThat(paymentResponseMono).isNotNull();
        PaymentResponse actual = paymentResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(PaymentResponse::getPaymentId)
                .isEqualTo(UUID.fromString("12fd4aa2-629f-463d-9114-15b095448a79"));
    }

    @Test
    @DisplayName("Verify that payment for enduring consent is retrieved")
    @Order(4)
    void getPaymentForEnduringConsent() throws ExpiredAccessTokenException {
        UUID paymentId = UUID.fromString("12fd4aa2-629f-463d-9114-15b095448a79");
        Mono<Payment> paymentMono = client.getPayment(UUID.randomUUID().toString(), accessToken, paymentId);

        assertThat(paymentMono).isNotNull();
        Payment actual = paymentMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Payment::getPaymentId, Payment::getType, Payment::getStatus, Payment::getRefunds)
                .containsExactly(paymentId, Payment.TypeEnum.ENDURING, Payment.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED,
                        Collections.emptyList());
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        PaymentRequest paymentRequest = actual.getDetail();
        assertThat(paymentRequest)
                .isNotNull()
                .extracting(PaymentRequest::getConsentId, PaymentRequest::getAccountReferenceId)
                .containsExactly(UUID.fromString("0500c560-c156-439f-9aed-753d82884323"), null);
        EnduringPaymentRequest enduringPaymentRequest = paymentRequest.getEnduringPayment();
        assertThat(enduringPaymentRequest).isNotNull();
        assertThat(enduringPaymentRequest.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "45.00");
        assertThat(enduringPaymentRequest.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
    }
}
