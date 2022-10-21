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

import io.netty.handler.logging.LogLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * The {@link Configuration} for Blink Debit.
 */
@Configuration
public class BlinkDebitConfiguration {

    @Value("${blinkpay.debit.url:}")
    private String debitUrl;

    @Value("${blinkpay.max.connections:50}")
    private int maxConnections;

    @Value("${blinkpay.max.idle.time:PT20S}")
    private Duration maxIdleTime;

    @Value("${blinkpay.max.life.time:PT60S}")
    private Duration maxLifeTime;

    @Value("${blinkpay.pending.acquire.timeout:PT10S}")
    private Duration pendingAcquireTimeout;

    @Value("${blinkpay.eviction.interval:PT60S}")
    private Duration evictionInterval;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    /**
     * Returns the {@link ReactorClientHttpConnector}.
     *
     * @return the {@link ReactorClientHttpConnector}
     */
    @Bean
    public ReactorClientHttpConnector blinkDebitClientHttpConnector() {
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
}
