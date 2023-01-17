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

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.netty.handler.logging.LogLevel;
import nz.co.blink.debit.exception.BlinkClientException;
import nz.co.blink.debit.exception.BlinkForbiddenException;
import nz.co.blink.debit.exception.BlinkNotImplementedException;
import nz.co.blink.debit.exception.BlinkRateLimitExceededException;
import nz.co.blink.debit.exception.BlinkRequestTimeoutException;
import nz.co.blink.debit.exception.BlinkResourceNotFoundException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.exception.BlinkUnauthorisedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import javax.validation.Validation;
import javax.validation.Validator;
import java.net.ConnectException;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * The {@link Configuration} for Blink Debit.
 */
@Configuration
@PropertySource(value = "classpath:blinkdebit.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:blinkdebit.yaml", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:blinkdebit.yml", ignoreResourceNotFound = true)
public class BlinkDebitConfiguration {

    @Value("${blinkpay.max.connections:10}")
    private int maxConnections;

    @Value("${blinkpay.max.idle.time:PT20S}")
    private Duration maxIdleTime;

    @Value("${blinkpay.max.life.time:PT60S}")
    private Duration maxLifeTime;

    @Value("${blinkpay.pending.acquire.timeout:PT10S}")
    private Duration pendingAcquireTimeout;

    @Value("${blinkpay.eviction.interval:PT60S}")
    private Duration evictionInterval;

    @Value("${spring.profiles.active:test}")
    private String activeProfile;

    @Value("${blinkpay.retry.enabled:true}")
    private Boolean retryEnabled;

    /**
     * Returns the {@link ReactorClientHttpConnector}.
     *
     * @return the {@link ReactorClientHttpConnector}
     */
    @Bean
    protected ReactorClientHttpConnector blinkDebitClientHttpConnector() {
        ConnectionProvider provider = ConnectionProvider.builder("blinkpay-conn-provider")
                .maxConnections(maxConnections)
                .maxIdleTime(maxIdleTime)
                .maxLifeTime(maxLifeTime)
                .pendingAcquireTimeout(pendingAcquireTimeout)
                .evictInBackground(evictionInterval)
                .build();

        HttpClient client;
        boolean debugMode = Pattern.compile("local|dev|test").matcher(activeProfile).matches();
        if (debugMode) {
            client = HttpClient.create(provider)
                    .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        } else {
            client = HttpClient.create(provider);
        }
        client.warmup().subscribe();

        return new ReactorClientHttpConnector(client);
    }

    /**
     * Returns the {@link Validator}.
     *
     * @return the {@link Validator}
     */
    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Returns the {@link Retry} instance.
     *
     * @return the {@link Retry} instance
     */
    @Bean
    public Retry retry() {
        if (Boolean.FALSE.equals(retryEnabled)) {
            return null;
        }

        RetryConfig retryConfig = RetryConfig.custom()
                // allow up to 2 retries after the original request (3 attempts in total)
                .maxAttempts(3)
                // wait 2 seconds and then 5 seconds (or thereabouts)
                .intervalFunction(IntervalFunction
                        .ofExponentialRandomBackoff(Duration.ofSeconds(2), 2, Duration.ofSeconds(3)))
                // retries are triggered for 408 (request timeout) and 5xx exceptions
                // and for network errors thrown by WebFlux if the request didn't get to the server at all
                .retryExceptions(BlinkRequestTimeoutException.class,
                        BlinkServiceException.class,
                        ConnectException.class,
                        WebClientRequestException.class)
                // ignore 4xx and 501 (not implemented) exceptions
                .ignoreExceptions(BlinkUnauthorisedException.class,
                        BlinkForbiddenException.class,
                        BlinkResourceNotFoundException.class,
                        BlinkRateLimitExceededException.class,
                        BlinkNotImplementedException.class,
                        BlinkClientException.class)
                .failAfterMaxAttempts(true)
                .build();

        return Retry.of("retry", retryConfig);
    }
}
