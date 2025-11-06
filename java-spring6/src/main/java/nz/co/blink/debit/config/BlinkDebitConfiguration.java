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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import nz.co.blink.debit.exception.BlinkRetryableException;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.service.ValidationService;
import nz.co.blink.debit.service.impl.JakartaValidationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.net.ConnectException;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * The internal {@link Configuration} for Blink Debit API client.
 */
@Configuration
@EnableConfigurationProperties(BlinkPayProperties.class)
public class BlinkDebitConfiguration {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_INITIAL_INTERVAL_SECONDS = 2L;
    private static final double RETRY_MULTIPLIER = 2.0;
    private static final long RETRY_RANDOMIZATION_FACTOR_SECONDS = 3L;

    @Value("${spring.profiles.active:test}")
    private String activeProfile;

    private final BlinkPayProperties properties;

    /**
     * Default constructor.
     *
     * @param properties the {@link BlinkPayProperties}
     */
    @Autowired
    public BlinkDebitConfiguration(BlinkPayProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns the {@link ReactorClientHttpConnector}.
     *
     * @return the {@link ReactorClientHttpConnector}
     */
    @Bean
    protected ReactorClientHttpConnector blinkDebitClientHttpConnector() {
        ConnectionProvider provider = ConnectionProvider.builder("blinkpay-conn-provider")
                .maxConnections(properties.getMax().getConnections())
                .maxIdleTime(properties.getMax().getIdle().getTime())
                .maxLifeTime(properties.getMax().getLife().getTime())
                .pendingAcquireTimeout(properties.getPending().getAcquire().getTimeout())
                .evictInBackground(properties.getEviction().getInterval())
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
    Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Returns the {@link ValidationService}.
     *
     * @return the {@link ValidationService}
     */
    @Bean
    public ValidationService validationService() {
        return new JakartaValidationServiceImpl(validator());
    }

    /**
     * Returns the {@link Retry} instance.
     *
     * @return the {@link Retry} instance
     */
    @Bean
    public Retry retry() {
        if (Boolean.FALSE.equals(properties.getRetry().getEnabled())) {
            return null;
        }

        RetryConfig retryConfig = RetryConfig.custom()
                // allow up to 2 retries after the original request (3 attempts in total)
                .maxAttempts(MAX_RETRY_ATTEMPTS)
                // wait 2 seconds and then 5 seconds (or thereabouts)
                .intervalFunction(IntervalFunction
                        .ofExponentialRandomBackoff(
                                Duration.ofSeconds(RETRY_INITIAL_INTERVAL_SECONDS),
                                RETRY_MULTIPLIER,
                                Duration.ofSeconds(RETRY_RANDOMIZATION_FACTOR_SECONDS)))
                // retries are triggered for 408 (request timeout) and 5xx exceptions
                // and for network errors thrown by WebFlux if the request didn't get to the server at all
                .retryExceptions(BlinkRetryableException.class,
                        ConnectException.class,
                        WebClientRequestException.class)
                // ignore 4xx and 501 (not implemented) exceptions
                .ignoreExceptions(BlinkServiceException.class)
                .failAfterMaxAttempts(true)
                .build();

        return Retry.of("retry", retryConfig);
    }
}
