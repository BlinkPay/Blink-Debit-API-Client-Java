/*
 * Copyright (c) 2025 BlinkPay
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.co.blink.debit.dto.v1;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;
import java.util.UUID;

/**
 * The details for an account_number refund request.
 * <p>
 * NOTE: This is a hand-written class to avoid circular reference issues.
 */
@JsonTypeName("account_number")
public class AccountNumberRefundRequest extends RefundDetail {

    public AccountNumberRefundRequest() {
        super();
    }

    public AccountNumberRefundRequest(UUID paymentId) {
        super(paymentId);
    }

    public AccountNumberRefundRequest paymentId(UUID paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountNumberRefundRequest that = (AccountNumberRefundRequest) o;
        return Objects.equals(paymentId, that.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return "AccountNumberRefundRequest{" +
                "paymentId=" + paymentId +
                '}';
    }
}
