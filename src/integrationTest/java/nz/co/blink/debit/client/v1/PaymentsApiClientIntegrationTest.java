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
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.EnduringPaymentRequest;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.Payment;
import nz.co.blink.debit.dto.v1.PaymentRequest;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.Period;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * The integration test for {@link PaymentsApiClient}.
 */
@SpringBootTest(classes = {OAuthApiClient.class, PaymentsApiClient.class, SingleConsentsApiClient.class,
        EnduringConsentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@ActiveProfiles("test")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentsApiClientIntegrationTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

    @Autowired
    private OAuthApiClient oAuthApiClient;

    @Autowired
    private EnduringConsentsApiClient enduringConsentsApiClient;

    @Autowired
    private SingleConsentsApiClient singleConsentsApiClient;

    @Autowired
    private PaymentsApiClient client;

    private static String accessToken;

    private static UUID consentId;

    private static UUID paymentId;

    @BeforeEach
    void setUp() {
        if (StringUtils.isBlank(accessToken)) {
            Mono<AccessTokenResponse> accessTokenResponseMono = oAuthApiClient.generateAccessToken(UUID.randomUUID().toString());

            assertThat(accessTokenResponseMono).isNotNull();
            AccessTokenResponse accessTokenResponse = accessTokenResponseMono.block();
            assertThat(accessTokenResponse).isNotNull();

            accessToken = accessTokenResponse.getAccessToken();
            assertThat(accessToken)
                    .isNotBlank()
                    .startsWith("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjdiZDdGYlpSdC1yT1oxVTRBaEdCbCJ9");
        }
    }

    @Test
    @DisplayName("Verify that payment for single consent with decoupled flow is created")
    @Order(1)
    void createPaymentForSingleConsentWithDecoupledFlow() throws ExpiredAccessTokenException, InterruptedException {
        Mono<CreateConsentResponse> createConsentResponseMono = singleConsentsApiClient.createSingleConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, REDIRECT_URI, "particulars", "code",
                "reference", "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425", null);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse createConsentResponse = createConsentResponseMono.block();
        assertThat(createConsentResponse).isNotNull();
        consentId = createConsentResponse.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(createConsentResponse.getRedirectUri()).isBlank();

        for (int i = 1; i <= 10; i++) {
            System.out.println("attempt: " + i);
            try {
                Mono<PaymentResponse> paymentResponseMono = client.createPayment(UUID.randomUUID().toString(),
                        accessToken, consentId);

                assertThat(paymentResponseMono).isNotNull();
                PaymentResponse actual = paymentResponseMono.block();
                assertThat(actual).isNotNull();
                paymentId = actual.getPaymentId();
                assertThat(paymentId).isNotNull();

                break;
            } catch (RuntimeException e) {
                // sleep incrementally
                Thread.sleep(2000 * i);
            }
        }

        if (paymentId == null) {
            fail("Payment for single consent with decoupled flow failed");
        }
    }

    @Test
    @DisplayName("Verify that payment for single consent with decoupled flow is retrieved")
    @Order(2)
    void getPaymentForSingleConsentWithDecoupledFlow() throws ExpiredAccessTokenException {
        if (paymentId == null) {
            fail("Payment ID from single consent with decoupled flow is null");
        }

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
                .containsExactly(consentId, null, null);
    }

    @Test
    @DisplayName("Verify that payment for enduring consent with decoupled flow is created")
    @Order(3)
    void createPaymentForEnduringConsentWithDecoupledFlow() throws ExpiredAccessTokenException, InterruptedException {
        Mono<CreateConsentResponse> createConsentResponseMono = enduringConsentsApiClient.createEnduringConsent(UUID.randomUUID().toString(),
                accessToken, AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null, Period.FORTNIGHTLY,
                OffsetDateTime.now(ZONE_ID), null, "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425", null);

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse createConsentResponse = createConsentResponseMono.block();
        assertThat(createConsentResponse).isNotNull();
        consentId = createConsentResponse.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(createConsentResponse.getRedirectUri()).isBlank();

        for (int i = 1; i <= 10; i++) {
            System.out.println("attempt: " + i);
            try {
                Mono<PaymentResponse> paymentResponseMono = client.createPayment(UUID.randomUUID().toString(),
                        accessToken, consentId, null, "particulars", "code", "reference", "45.00");

                assertThat(paymentResponseMono).isNotNull();
                PaymentResponse actual = paymentResponseMono.block();
                assertThat(actual).isNotNull();
                paymentId = actual.getPaymentId();
                assertThat(paymentId).isNotNull();

                break;
            } catch (RuntimeException e) {
                // sleep incrementally
                Thread.sleep(2000 * i);
            }
        }

        if (paymentId == null) {
            fail("Payment for enduring consent with decoupled flow failed");
        }
    }

    @Test
    @DisplayName("Verify that payment for enduring consent with decoupled flow is retrieved")
    @Order(4)
    void getPaymentForEnduringConsentWithDecoupledFlow() throws ExpiredAccessTokenException {
        if (paymentId == null) {
            fail("Payment ID from enduring consent with decoupled flow is null");
        }

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
                .containsExactly(consentId, null);
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
