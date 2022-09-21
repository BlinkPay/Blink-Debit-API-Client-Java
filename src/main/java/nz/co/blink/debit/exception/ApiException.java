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
package nz.co.blink.debit.exception;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * The base API exception.
 */
@Getter
@ToString
public class ApiException extends Exception {

    private final ApiError apiError;

    /**
     * Constructor with {@link HttpStatus}, reason and {@link Throwable}.
     *
     * @param status the {@link HttpStatus}
     * @param reason the error message
     * @param cause  the {@link Throwable}
     */
    public ApiException(HttpStatus status, final String reason, Throwable cause) {
        super(reason, cause);
        apiError = ApiError.builder()
                .status(status)
                .reason(reason)
                .cause(cause)
                .build();
    }

    /**
     * Constructor with {@link HttpStatus}, reason, {@link Throwable} and description.
     *
     * @param status the {@link HttpStatus}
     * @param reason the error message
     * @param cause  the {@link Throwable}
     * @param description the description
     */
    public ApiException(HttpStatus status, final String reason, Throwable cause, final String description) {
        super(reason, cause);
        apiError = ApiError.builder()
                .status(status)
                .reason(reason)
                .cause(cause)
                .description(description)
                .build();
    }

    /**
     * Constructor with {@link HttpStatus}, reason, {@link Throwable}, description and error code.
     *
     * @param status the {@link HttpStatus}
     * @param reason the error message
     * @param cause  the {@link Throwable}
     * @param description the description
     * @param errorCode the error code
     */
    public ApiException(HttpStatus status, final String reason, Throwable cause, final String description,
                        final String errorCode) {
        super(reason, cause);
        apiError = ApiError.builder()
                .status(status)
                .reason(reason)
                .cause(cause)
                .description(description)
                .code(errorCode)
                .build();
    }
}
