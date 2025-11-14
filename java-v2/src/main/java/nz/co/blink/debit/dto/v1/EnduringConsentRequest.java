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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * The model for an enduring consent request, relating to multiple payments.
 * <p>
 * NOTE: This is a hand-written class to avoid circular reference issues.
 */
@JsonTypeName("enduring")
public class EnduringConsentRequest extends ConsentDetail {

    @NotNull
    @Valid
    @JsonProperty("flow")
    private AuthFlow flow;

    @NotNull
    @Valid
    @JsonProperty("from_timestamp")
    private OffsetDateTime fromTimestamp;

    @Valid
    @JsonProperty("expiry_timestamp")
    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    private OffsetDateTime expiryTimestamp;

    @NotNull
    @Valid
    @JsonProperty("period")
    private Period period;

    @NotNull
    @Valid
    @JsonProperty("maximum_amount_period")
    private Amount maximumAmountPeriod;

    @Valid
    @JsonProperty("maximum_amount_payment")
    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    private Amount maximumAmountPayment;

    @JsonProperty("hashed_customer_identifier")
    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    private String hashedCustomerIdentifier;

    public EnduringConsentRequest() {
    }

    public EnduringConsentRequest flow(AuthFlow flow) {
        this.flow = flow;
        return this;
    }

    /**
     * Get flow
     *
     * @return flow
     */
    public AuthFlow getFlow() {
        return flow;
    }

    public void setFlow(AuthFlow flow) {
        this.flow = flow;
    }

    public EnduringConsentRequest fromTimestamp(OffsetDateTime fromTimestamp) {
        this.fromTimestamp = fromTimestamp;
        return this;
    }

    /**
     * The ISO 8601 start date to calculate the periods for which to calculate the consent period.
     *
     * @return fromTimestamp
     */
    public OffsetDateTime getFromTimestamp() {
        return fromTimestamp;
    }

    public void setFromTimestamp(OffsetDateTime fromTimestamp) {
        this.fromTimestamp = fromTimestamp;
    }

    public EnduringConsentRequest expiryTimestamp(OffsetDateTime expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
        return this;
    }

    /**
     * The ISO 8601 timeout for when an enduring consent will expire.
     * If this field is blank, an indefinite request will be attempted.
     *
     * @return expiryTimestamp
     */
    public OffsetDateTime getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(OffsetDateTime expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }

    public EnduringConsentRequest period(Period period) {
        this.period = period;
        return this;
    }

    /**
     * Get period
     *
     * @return period
     */
    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public EnduringConsentRequest maximumAmountPeriod(Amount maximumAmountPeriod) {
        this.maximumAmountPeriod = maximumAmountPeriod;
        return this;
    }

    /**
     * Get maximumAmountPeriod
     *
     * @return maximumAmountPeriod
     */
    public Amount getMaximumAmountPeriod() {
        return maximumAmountPeriod;
    }

    public void setMaximumAmountPeriod(Amount maximumAmountPeriod) {
        this.maximumAmountPeriod = maximumAmountPeriod;
    }

    public EnduringConsentRequest maximumAmountPayment(Amount maximumAmountPayment) {
        this.maximumAmountPayment = maximumAmountPayment;
        return this;
    }

    /**
     * Get maximumAmountPayment
     *
     * @return maximumAmountPayment
     */
    public Amount getMaximumAmountPayment() {
        return maximumAmountPayment;
    }

    public void setMaximumAmountPayment(Amount maximumAmountPayment) {
        this.maximumAmountPayment = maximumAmountPayment;
    }

    public EnduringConsentRequest hashedCustomerIdentifier(String hashedCustomerIdentifier) {
        this.hashedCustomerIdentifier = hashedCustomerIdentifier;
        return this;
    }

    /**
     * The hashed unique ID of the customer e.g. customer internal ID. SHA-256 is recommended.
     *
     * @return hashedCustomerIdentifier
     */
    public String getHashedCustomerIdentifier() {
        return hashedCustomerIdentifier;
    }

    public void setHashedCustomerIdentifier(String hashedCustomerIdentifier) {
        this.hashedCustomerIdentifier = hashedCustomerIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnduringConsentRequest that = (EnduringConsentRequest) o;
        return Objects.equals(flow, that.flow) &&
                Objects.equals(fromTimestamp, that.fromTimestamp) &&
                Objects.equals(expiryTimestamp, that.expiryTimestamp) &&
                Objects.equals(period, that.period) &&
                Objects.equals(maximumAmountPeriod, that.maximumAmountPeriod) &&
                Objects.equals(maximumAmountPayment, that.maximumAmountPayment) &&
                Objects.equals(hashedCustomerIdentifier, that.hashedCustomerIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flow, fromTimestamp, expiryTimestamp, period,
                maximumAmountPeriod, maximumAmountPayment, hashedCustomerIdentifier);
    }

    @Override
    public String toString() {
        return "EnduringConsentRequest{" +
                "flow=" + flow +
                ", fromTimestamp=" + fromTimestamp +
                ", expiryTimestamp=" + expiryTimestamp +
                ", period=" + period +
                ", maximumAmountPeriod=" + maximumAmountPeriod +
                ", maximumAmountPayment=" + maximumAmountPayment +
                ", hashedCustomerIdentifier='" + hashedCustomerIdentifier + '\'' +
                '}';
    }
}
