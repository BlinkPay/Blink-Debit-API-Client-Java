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
import nz.co.blink.debit.dto.v1.AccountNumberRefundRequest;
import nz.co.blink.debit.dto.v1.Amount;
import nz.co.blink.debit.dto.v1.FullRefundRequest;
import nz.co.blink.debit.dto.v1.PartialRefundRequest;
import nz.co.blink.debit.dto.v1.Pcr;
import nz.co.blink.debit.dto.v1.Refund;
import nz.co.blink.debit.dto.v1.RefundDetail;
import nz.co.blink.debit.dto.v1.RefundResponse;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The component test case for {@link RefundsApiClient}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.profiles.active=component"},
        classes = {AccessTokenHandler.class, OAuthApiClient.class, RefundsApiClient.class})
@Import(BlinkDebitConfiguration.class)
@AutoConfigureWireMock(port = 8888,
        stubs = "file:src/componentTest/resources/wiremock/mappings",
        files = "file:src/componentTest/resources/wiremock")
@ActiveProfiles("component")
@Tag("component")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RefundsApiClientComponentTest {

    @Autowired
    private ReactorClientHttpConnector connector;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private BlinkPayProperties properties;

    private RefundsApiClient client;

    @BeforeEach
    void setUp() {
        // use real host to generate valid access token
        BlinkPayProperties blinkPayProperties = new BlinkPayProperties();
        blinkPayProperties.getDebit().setUrl("https://sandbox.debit.blinkpay.co.nz");
        blinkPayProperties.getClient().setId("mock-client-id");
        blinkPayProperties.getClient().setSecret("mock-client-secret");
        OAuthApiClient oauthApiClient = new OAuthApiClient(connector, blinkPayProperties);

        client = new RefundsApiClient(connector, properties, new AccessTokenHandler(oauthApiClient), validationService,
                retry);
    }

    @Test
    @DisplayName("Verify that account number refund is created")
    @Order(1)
    void createAccountNumberRefund() throws BlinkServiceException {
        AccountNumberRefundRequest request = (AccountNumberRefundRequest) new AccountNumberRefundRequest()
                .paymentId(UUID.fromString("76ac9fa3-4793-45fe-8682-c7876fc5262e"));

        Mono<RefundResponse> refundResponseMono = client.createRefund(request);

        assertThat(refundResponseMono).isNotNull();
        RefundResponse actual = refundResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(RefundResponse::getRefundId)
                .isEqualTo(UUID.fromString("e9cc86bf-3030-4019-a0f5-1d73882051ea"));
    }

    @Test
    @DisplayName("Verify that account number refund is retrieved")
    @Order(2)
    void getAccountNumberRefund() throws BlinkServiceException {
        UUID refundId = UUID.fromString("e9cc86bf-3030-4019-a0f5-1d73882051ea");
        Mono<Refund> refundMono = client.getRefund(refundId);

        assertThat(refundMono).isNotNull();
        Refund actual = refundMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Refund::getRefundId, Refund::getStatus, Refund::getAccountNumber)
                .containsExactly(refundId, Refund.StatusEnum.COMPLETED, "99-6121-3292712-00");
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail()).isNotNull();
        AccountNumberRefundRequest refundDetail = (AccountNumberRefundRequest) actual.getDetail();
        assertThat(refundDetail)
                .isNotNull()
                .extracting(AccountNumberRefundRequest::getPaymentId, AccountNumberRefundRequest::getType)
                .containsExactly(UUID.fromString("76ac9fa3-4793-45fe-8682-c7876fc5262e"),
                        RefundDetail.TypeEnum.ACCOUNT_NUMBER);
    }

    @Test
    @DisplayName("Verify that full refund is created")
    @Order(3)
    void createFullRefund() throws BlinkServiceException {
        FullRefundRequest request = (FullRefundRequest) new FullRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .paymentId(UUID.fromString("5de1b67f-0214-462e-aab5-1d8397b2fe67"));

        Mono<RefundResponse> refundResponseMono = client.createRefund(request);

        assertThat(refundResponseMono).isNotNull();
        RefundResponse actual = refundResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(RefundResponse::getRefundId)
                .isEqualTo(UUID.fromString("87c203e9-00ff-40d8-ad90-7bdd67895731"));
    }

    @Test
    @DisplayName("Verify that full refund is retrieved")
    @Order(4)
    void getFullRefund() throws BlinkServiceException {
        UUID refundId = UUID.fromString("87c203e9-00ff-40d8-ad90-7bdd67895731");
        Mono<Refund> refundMono = client.getRefund(refundId);

        assertThat(refundMono).isNotNull();
        Refund actual = refundMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Refund::getRefundId, Refund::getStatus, Refund::getAccountNumber)
                .containsExactly(refundId, Refund.StatusEnum.COMPLETED, "99-6121-3292712-00");
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail()).isNotNull();
        FullRefundRequest refundDetail = (FullRefundRequest) actual.getDetail();
        assertThat(refundDetail)
                .isNotNull()
                .extracting(FullRefundRequest::getPaymentId,
                        FullRefundRequest::getType, FullRefundRequest::getConsentRedirect)
                .containsExactly(UUID.fromString("5de1b67f-0214-462e-aab5-1d8397b2fe67"),
                        RefundDetail.TypeEnum.FULL_REFUND, "http://localhost:8888/callback");
        assertThat(refundDetail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
    }

    @Test
    @DisplayName("Verify that partial refund is created")
    @Order(5)
    void createPartialRefund() throws BlinkServiceException {
        PartialRefundRequest request = (PartialRefundRequest) new PartialRefundRequest()
                .consentRedirect("https://www.mymerchant.co.nz")
                .pcr(new Pcr()
                        .particulars("particulars")
                        .code("code")
                        .reference("reference"))
                .amount(new Amount()
                        .currency(Amount.CurrencyEnum.NZD)
                        .total("25.00"))
                .paymentId(UUID.fromString("3df492b7-19ee-4094-b91c-dc20e449e436"));

        Mono<RefundResponse> refundResponseMono = client.createRefund(request);

        assertThat(refundResponseMono).isNotNull();
        RefundResponse actual = refundResponseMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(RefundResponse::getRefundId)
                .isEqualTo(UUID.fromString("573ac992-f154-48fd-9234-349d833cc6ec"));
    }

    @Test
    @DisplayName("Verify that partial refund is retrieved")
    @Order(6)
    void getPartialRefund() throws BlinkServiceException {
        UUID refundId = UUID.fromString("3df492b7-19ee-4094-b91c-dc20e449e436");
        Mono<Refund> refundMono = client.getRefund(refundId);

        assertThat(refundMono).isNotNull();
        Refund actual = refundMono.block();
        assertThat(actual)
                .isNotNull()
                .extracting(Refund::getRefundId, Refund::getStatus, Refund::getAccountNumber)
                .containsExactly(refundId, Refund.StatusEnum.COMPLETED, "99-6121-3292712-00");
        assertThat(actual.getCreationTimestamp()).isNotNull();
        assertThat(actual.getStatusUpdatedTimestamp()).isNotNull();
        assertThat(actual.getDetail()).isNotNull();
        PartialRefundRequest refundDetail = (PartialRefundRequest) actual.getDetail();
        assertThat(refundDetail)
                .isNotNull()
                .extracting(PartialRefundRequest::getPaymentId,
                        PartialRefundRequest::getType, PartialRefundRequest::getConsentRedirect)
                .containsExactly(UUID.fromString("573ac992-f154-48fd-9234-349d833cc6ec"),
                        RefundDetail.TypeEnum.PARTIAL_REFUND, "http://localhost:8888/callback");
        assertThat(refundDetail.getPcr())
                .isNotNull()
                .extracting(Pcr::getParticulars, Pcr::getCode, Pcr::getReference)
                .containsExactly("particulars", "code", "reference");
        assertThat(refundDetail.getAmount())
                .isNotNull()
                .extracting(Amount::getCurrency, Amount::getTotal)
                .containsExactly(Amount.CurrencyEnum.NZD, "25.00");
    }
}
