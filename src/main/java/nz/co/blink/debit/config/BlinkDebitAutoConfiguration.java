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
package nz.co.blink.debit.config;

import io.github.resilience4j.retry.Retry;
import nz.co.blink.debit.client.v1.BlinkDebitClient;
import nz.co.blink.debit.client.v1.EnduringConsentsApiClient;
import nz.co.blink.debit.client.v1.MetaApiClient;
import nz.co.blink.debit.client.v1.OAuthApiClient;
import nz.co.blink.debit.client.v1.PaymentsApiClient;
import nz.co.blink.debit.client.v1.QuickPaymentsApiClient;
import nz.co.blink.debit.client.v1.RefundsApiClient;
import nz.co.blink.debit.client.v1.SingleConsentsApiClient;
import nz.co.blink.debit.helpers.AccessTokenHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import javax.validation.Validator;

/**
 * The {@link AutoConfiguration} for Spring-based consumers.
 */
@AutoConfiguration
@Import(BlinkDebitConfiguration.class)
public class BlinkDebitAutoConfiguration {

    @Autowired
    @Qualifier("blinkDebitClientHttpConnector")
    private ReactorClientHttpConnector connector;

    @Value("${blinkpay.debit.url:}")
    private String debitUrl;

    @Value("${blinkpay.client.id:}")
    private String clientId;

    @Value("${blinkpay.client.secret:}")
    private String clientSecret;

    @Autowired
    private Validator validator;

    @Autowired
    private Retry retry;

    @Bean
    private OAuthApiClient oauthApiClient() {
        return new OAuthApiClient(connector, debitUrl, clientId, clientSecret, retry);
    }

    @Bean
    private AccessTokenHandler accessTokenHandler() {
        return new AccessTokenHandler(oauthApiClient());
    }

    @Bean
    public SingleConsentsApiClient singleConsentsApiClient() {
        return new SingleConsentsApiClient(connector, debitUrl, accessTokenHandler(), validator, retry);
    }

    @Bean
    public EnduringConsentsApiClient enduringConsentsApiClient() {
        return new EnduringConsentsApiClient(connector, debitUrl, accessTokenHandler(), validator, retry);
    }

    @Bean
    public QuickPaymentsApiClient quickPaymentsApiClient() {
        return new QuickPaymentsApiClient(connector, debitUrl, accessTokenHandler(), validator, retry);
    }

    @Bean
    public PaymentsApiClient paymentsApiClient() {
        return new PaymentsApiClient(connector, debitUrl, accessTokenHandler(), validator, retry);
    }

    @Bean
    public RefundsApiClient refundsApiClient() {
        return new RefundsApiClient(connector, debitUrl, accessTokenHandler(), validator, retry);
    }

    @Bean
    public MetaApiClient metaApiClient() {
        return new MetaApiClient(connector, debitUrl, accessTokenHandler());
    }

    @Bean
    public BlinkDebitClient blinkDebitClient() {
        return new BlinkDebitClient(singleConsentsApiClient(), enduringConsentsApiClient(), quickPaymentsApiClient(),
                paymentsApiClient(), refundsApiClient(), metaApiClient(), validator, retry);
    }
}
