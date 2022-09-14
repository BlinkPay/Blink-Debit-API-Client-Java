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
package nz.co.blink.debit.helpers;

import lombok.extern.slf4j.Slf4j;
import nz.co.blink.debit.dto.v1.DetailErrorResponseModel;
import nz.co.blink.debit.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The helper class for handling HTTP responses.
 */
@Slf4j
public final class ResponseHandler {

    /**
     * Private constructor.
     */
    private ResponseHandler() {
        // prevent instantiation
    }

    /**
     * Handles a {@link Mono} response.
     *
     * @param clazz the type of the {@link Mono}
     * @param <T>   the type
     * @return the {@link Function}
     */
    public static <T> Function<ClientResponse, Mono<T>> getResponseMono(Class<T> clazz) {
        return clientResponse -> {
            if (clientResponse.statusCode().is2xxSuccessful()) {
                return clientResponse
                        .bodyToMono(clazz);
            } else if (clientResponse.statusCode().isError()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(new ApiException(clientResponse.statusCode(), null, null, null)))
                        .flatMap(body -> {
                            log.error("{} | Encountered: {}", clientResponse.headers().header(REQUEST_ID.getValue()),
                                    body.getMessage());
                            return Mono.error(new ApiException(HttpStatus.valueOf(body.getStatus()), body.getError(),
                                    null, body.getMessage(), body.getCode()));
                        });
            } else {
                return clientResponse
                        .createException()
                        .flatMap(error -> {
                            log.error("{} | Encountered an unknown error: {}",
                                    clientResponse.headers().header(REQUEST_ID.getValue()), error.getMessage());
                            return Mono.error(error);
                        });
            }
        };
    }

    /**
     * Handles a {@link Flux} response.
     *
     * @param clazz the type of the {@link Flux}
     * @param <T>   the type
     * @return the {@link Function}
     */
    public static <T> Function<ClientResponse, Flux<T>> getResponseFlux(Class<T> clazz) {
        return clientResponse -> {
            if (clientResponse.statusCode().is2xxSuccessful()) {
                return clientResponse
                        .bodyToFlux(clazz);
            } else if (clientResponse.statusCode().isError()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(new ApiException(clientResponse.statusCode(), null, null, null)))
                        .flatMap(body -> {
                            log.error("{} | Encountered: {}", clientResponse.headers().header(REQUEST_ID.getValue()),
                                    body.getMessage());
                            return Mono.error(new ApiException(HttpStatus.valueOf(body.getStatus()), body.getError(),
                                    null, body.getMessage(), body.getCode()));
                        });
            } else {
                return clientResponse
                        .createException()
                        .flux()
                        .flatMap(error -> {
                            log.error("{} | Encountered an unknown error: {}",
                                    clientResponse.headers().header(REQUEST_ID.getValue()), error.getMessage());
                            return Mono.error(error);
                        });
            }
        };
    }
}
