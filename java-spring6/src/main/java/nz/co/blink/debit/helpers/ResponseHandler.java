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
import nz.co.blink.debit.exception.BlinkRetryableException;
import nz.co.blink.debit.exception.BlinkServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static nz.co.blink.debit.enums.BlinkDebitConstant.REQUEST_ID;

/**
 * The helper class for handling HTTP responses.
 */
@Slf4j
public final class ResponseHandler {

    private static final String RESPONSE_FORMAT = "Status Code: {}\nHeaders: {}\nBody: {}";

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
    public static <T> Function<ClientResponse, Mono<T>> handleResponseMono(Class<T> clazz) {
        return clientResponse -> {
            if (clientResponse.statusCode().is2xxSuccessful()) {
                return clientResponse
                        .bodyToMono(clazz);
            } else if (HttpStatus.UNAUTHORIZED == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createUnauthorisedException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.UNPROCESSABLE_ENTITY == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createUnauthorisedException()))
                        .flatMap(body -> {
                            log.warn(RESPONSE_FORMAT, clientResponse.statusCode(), clientResponse.headers(),
                                    body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.FORBIDDEN == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createForbiddenException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.NOT_FOUND == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createResourceNotFoundException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.REQUEST_TIMEOUT == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(new BlinkRetryableException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(new BlinkRetryableException(body.getMessage()));
                        });
            } else if (HttpStatus.TOO_MANY_REQUESTS == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createRateLimitExceededException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.NOT_IMPLEMENTED == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createServiceNotImplementedException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createClientException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse
                        .bodyToMono(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(new BlinkRetryableException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(new BlinkRetryableException(body.getMessage()));
                        });
            } else {
                return clientResponse
                        .createException()
                        .flatMap(error -> {
                            List<String> requestId = clientResponse.headers().header(REQUEST_ID.getValue());
                            String errorMessage = error.getMessage();
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), errorMessage);
                            return Mono.error(BlinkServiceException.createServiceException("Service call to Blink Debit failed with error: "
                                    + errorMessage + ", please contact BlinkPay with the request ID: "
                                    + requestId));
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
    public static <T> Function<ClientResponse, Flux<T>> handleResponseFlux(Class<T> clazz) {
        return clientResponse -> {
            if (clientResponse.statusCode().is2xxSuccessful()) {
                return clientResponse
                        .bodyToFlux(clazz);
            } else if (HttpStatus.UNAUTHORIZED == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createUnauthorisedException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(), clientResponse.headers(),
                                    body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.UNPROCESSABLE_ENTITY == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createUnauthorisedException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(), clientResponse.headers(),
                                    body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.FORBIDDEN == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createForbiddenException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.NOT_FOUND == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createResourceNotFoundException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.REQUEST_TIMEOUT == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(new BlinkRetryableException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(new BlinkRetryableException(body.getMessage()));
                        });
            } else if (HttpStatus.TOO_MANY_REQUESTS == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createRateLimitExceededException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (HttpStatus.NOT_IMPLEMENTED == clientResponse.statusCode()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createServiceNotImplementedException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(BlinkServiceException.createClientException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(BlinkServiceException.createServiceException(body.getMessage()));
                        });
            } else if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse
                        .bodyToFlux(DetailErrorResponseModel.class)
                        .switchIfEmpty(Mono.error(new BlinkRetryableException()))
                        .flatMap(body -> {
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), body.getMessage());
                            return Mono.error(new BlinkRetryableException(body.getMessage()));
                        });
            } else {
                return clientResponse
                        .createException()
                        .flux()
                        .flatMap(error -> {
                            List<String> requestId = clientResponse.headers().header(REQUEST_ID.getValue());
                            String errorMessage = error.getMessage();
                            log.error(RESPONSE_FORMAT, clientResponse.statusCode(),
                                    clientResponse.headers().asHttpHeaders(), errorMessage);
                            return Mono.error(BlinkServiceException.createServiceException("Service call to Blink Debit failed with error: "
                                    + errorMessage + ", please contact BlinkPay with the correlation ID: "
                                    + requestId));
                        });
            }
        };
    }
}
