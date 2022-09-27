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
import nz.co.blink.debit.dto.v1.AuthFlowDetail;
import nz.co.blink.debit.dto.v1.Bank;
import nz.co.blink.debit.dto.v1.CreateConsentResponse;
import nz.co.blink.debit.dto.v1.IdentifierType;
import nz.co.blink.debit.dto.v1.PaymentResponse;
import nz.co.blink.debit.dto.v1.Period;
import nz.co.blink.debit.dto.v1.Refund;
import nz.co.blink.debit.dto.v1.RefundDetail;
import nz.co.blink.debit.dto.v1.RefundResponse;
import nz.co.blink.debit.exception.ApiError;
import nz.co.blink.debit.exception.ApiException;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.fail;

/**
 * The integration test for {@link RefundsApiClient}.
 */
@SpringBootTest(classes = {AccessTokenHandler.class, OAuthApiClient.class, RefundsApiClient.class,
        PaymentsApiClient.class, SingleConsentsApiClient.class, EnduringConsentsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@ActiveProfiles("test")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RefundsApiClientIntegrationTest {

    private static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";

    private static final ZoneId ZONE_ID = ZoneId.of("Pacific/Auckland");

    @Autowired
    private EnduringConsentsApiClient enduringConsentsApiClient;

    @Autowired
    private SingleConsentsApiClient singleConsentsApiClient;

    @Autowired
    private PaymentsApiClient paymentsApiClient;

    @Autowired
    private RefundsApiClient client;

    private static UUID consentId;

    private static UUID paymentId;

    private static UUID refundId;

    @Test
    @DisplayName("Verify that account refund for single consent with decoupled flow is created")
    @Order(1)
    void createAccountNumberRefundForSingleConsentWithDecoupledFlow()
            throws InterruptedException {
        Mono<CreateConsentResponse> createConsentResponseMono =
                singleConsentsApiClient.createSingleConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, REDIRECT_URI,
                        "particulars", "code", "reference", "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425",
                        "https://eout2fipbfh7o93.m.pipedream.net");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse createConsentResponse = createConsentResponseMono.block();
        assertThat(createConsentResponse).isNotNull();
        consentId = createConsentResponse.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(createConsentResponse.getRedirectUri()).isBlank();

        for (int i = 1; i <= 10; i++) {
            System.out.println("attempt: " + i);
            try {
                Mono<PaymentResponse> paymentResponseMono = paymentsApiClient.createPayment(consentId);

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

        Mono<RefundResponse> refundResponseMono = client.createRefund(RefundDetail.TypeEnum.ACCOUNT_NUMBER, paymentId);

        assertThat(refundResponseMono).isNotNull();
        RefundResponse actual = refundResponseMono.block();
        assertThat(actual).isNotNull();
        refundId = actual.getRefundId();
        assertThat(refundId).isNotNull();
    }

    @Test
    @DisplayName("Verify that account number refund for single consent with decoupled flow is retrieved")
    @Order(2)
    void getAccountNumberRefundForSingleConsentWithDecoupledFlow() {
        if (refundId == null) {
            fail("Refund ID from single consent with decoupled flow is null");
        }

        Mono<Refund> refundMono = client.getRefund(refundId, UUID.randomUUID().toString());

        assertThat(refundMono).isNotNull();
        Refund actual = refundMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Refund::getRefundId, Refund::getStatus, Refund::getAccountNumber)
                .containsExactly(refundId, Refund.StatusEnum.COMPLETED, "99-6121-6242460-00");
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        RefundDetail refundDetail = (RefundDetail) actual.getDetail();
        assertThat(refundDetail)
                .isNotNull()
                .extracting(RefundDetail::getPaymentId, RefundDetail::getType)
                .containsExactly(paymentId, RefundDetail.TypeEnum.ACCOUNT_NUMBER);
    }

    @Test
    @DisplayName("Verify that account number refund for enduring consent with decoupled flow is created")
    @Order(3)
    void createAccountNumberRefundForEnduringConsentWithDecoupledFlow()
            throws InterruptedException {
        Mono<CreateConsentResponse> createConsentResponseMono =
                enduringConsentsApiClient.createEnduringConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, null,
                        Period.FORTNIGHTLY, OffsetDateTime.now(ZONE_ID), null, "50.00", null,
                        IdentifierType.PHONE_NUMBER, "+6449144425", "https://eout2fipbfh7o93.m.pipedream.net");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse createConsentResponse = createConsentResponseMono.block();
        assertThat(createConsentResponse).isNotNull();
        consentId = createConsentResponse.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(createConsentResponse.getRedirectUri()).isBlank();

        for (int i = 1; i <= 10; i++) {
            System.out.println("attempt: " + i);
            try {
                Mono<PaymentResponse> paymentResponseMono = paymentsApiClient.createPayment(consentId, "particulars",
                        "code", "reference", "45.00");

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

        Mono<RefundResponse> refundResponseMono = client.createRefund(RefundDetail.TypeEnum.ACCOUNT_NUMBER, paymentId);

        assertThat(refundResponseMono).isNotNull();
        RefundResponse actual = refundResponseMono.block();
        assertThat(actual).isNotNull();
        refundId = actual.getRefundId();
        assertThat(refundId).isNotNull();
    }

    @Test
    @DisplayName("Verify that account number refund for enduring consent with decoupled flow is retrieved")
    @Order(4)
    void getAccountNumberRefundForEnduringConsentWithDecoupledFlow() {
        if (refundId == null) {
            fail("Refund ID from enduring consent with decoupled flow is null");
        }

        Mono<Refund> refundMono = client.getRefund(refundId, UUID.randomUUID().toString());

        assertThat(refundMono).isNotNull();
        Refund actual = refundMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Refund::getRefundId, Refund::getStatus, Refund::getAccountNumber)
                .containsExactly(refundId, Refund.StatusEnum.COMPLETED, "99-6121-6242460-00");
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        RefundDetail refundDetail = (RefundDetail) actual.getDetail();
        assertThat(refundDetail)
                .isNotNull()
                .extracting(RefundDetail::getPaymentId, RefundDetail::getType)
                .containsExactly(paymentId, RefundDetail.TypeEnum.ACCOUNT_NUMBER);
    }

    @Test
    @DisplayName("Verify that full refund for single consent with decoupled flow is handled")
    @Order(5)
    @Disabled("re-enable when staging returns 501 instead of 403")
    void createFullRefundForSingleConsentWithDecoupledFlow() throws InterruptedException {
        Mono<CreateConsentResponse> createConsentResponseMono =
                singleConsentsApiClient.createSingleConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, REDIRECT_URI,
                        "particulars", "code", "reference", "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425",
                        "https://eout2fipbfh7o93.m.pipedream.net");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse createConsentResponse = createConsentResponseMono.block();
        assertThat(createConsentResponse).isNotNull();
        consentId = createConsentResponse.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(createConsentResponse.getRedirectUri()).isBlank();

        for (int i = 1; i <= 10; i++) {
            System.out.println("attempt: " + i);
            try {
                Mono<PaymentResponse> paymentResponseMono = paymentsApiClient.createPayment(consentId);

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

        RuntimeException exception = catchThrowableOfType(() -> client.createRefund(RefundDetail.TypeEnum.FULL_REFUND,
                paymentId, "http://localhost", "particulars1", "code1", "reference1").block(), RuntimeException.class);

        assertThat(exception).isNotNull();
        assertThat(((ApiException) exception.getCause()).getApiError())
                .isNotNull()
                .extracting(ApiError::getStatus, ApiError::getCode, ApiError::getReason, ApiError::getCause,
                        ApiError::getDescription)
                .containsExactly(HttpStatus.NOT_IMPLEMENTED, null, "NOT_IMPLEMENTED", null,
                        "Full refund is not yet implemented");
    }

    @Test
    @DisplayName("Verify that partial refund for single consent with decoupled flow is handled")
    @Order(6)
    @Disabled("re-enable when staging returns 501 instead of 403")
    void createPartialRefundForSingleConsentWithDecoupledFlow()
            throws InterruptedException {
        Mono<CreateConsentResponse> createConsentResponseMono =
                singleConsentsApiClient.createSingleConsent(AuthFlowDetail.TypeEnum.DECOUPLED, Bank.PNZ, REDIRECT_URI,
                        "particulars", "code", "reference", "50.00", null, IdentifierType.PHONE_NUMBER, "+6449144425",
                        "https://eout2fipbfh7o93.m.pipedream.net");

        assertThat(createConsentResponseMono).isNotNull();
        CreateConsentResponse createConsentResponse = createConsentResponseMono.block();
        assertThat(createConsentResponse).isNotNull();
        consentId = createConsentResponse.getConsentId();
        assertThat(consentId).isNotNull();
        assertThat(createConsentResponse.getRedirectUri()).isBlank();

        for (int i = 1; i <= 10; i++) {
            System.out.println("attempt: " + i);
            try {
                Mono<PaymentResponse> paymentResponseMono = paymentsApiClient.createPayment(consentId);

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

        RuntimeException exception = catchThrowableOfType(() ->
                client.createRefund(RefundDetail.TypeEnum.PARTIAL_REFUND, paymentId, "http://localhost", "particulars1",
                        "code1", "reference1", "25.00", null).block(), RuntimeException.class);

        assertThat(exception).isNotNull();
        assertThat(((ApiException) exception.getCause()).getApiError())
                .isNotNull()
                .extracting(ApiError::getStatus, ApiError::getCode, ApiError::getReason, ApiError::getCause,
                        ApiError::getDescription)
                .containsExactly(HttpStatus.NOT_IMPLEMENTED, null, "NOT_IMPLEMENTED", null,
                        "Partial refund is not yet implemented");
    }
}
