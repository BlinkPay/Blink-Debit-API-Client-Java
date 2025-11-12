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
import java.util.Objects;

/**
 * The model for a single consent request, relating to a one-off payment.
 * <p>
 * NOTE: This is a hand-written class to avoid circular reference issues.
 */
@JsonTypeName("single")
public class SingleConsentRequest extends ConsentDetail {

    @NotNull
    @Valid
    @JsonProperty("flow")
    private AuthFlow flow;

    @NotNull
    @Valid
    @JsonProperty("pcr")
    private Pcr pcr;

    @NotNull
    @Valid
    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("hashed_customer_identifier")
    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    private String hashedCustomerIdentifier;

    public SingleConsentRequest() {
    }

    public SingleConsentRequest flow(AuthFlow flow) {
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

    public SingleConsentRequest pcr(Pcr pcr) {
        this.pcr = pcr;
        return this;
    }

    /**
     * Get pcr
     *
     * @return pcr
     */
    public Pcr getPcr() {
        return pcr;
    }

    public void setPcr(Pcr pcr) {
        this.pcr = pcr;
    }

    public SingleConsentRequest amount(Amount amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Get amount
     *
     * @return amount
     */
    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public SingleConsentRequest hashedCustomerIdentifier(String hashedCustomerIdentifier) {
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
        SingleConsentRequest that = (SingleConsentRequest) o;
        return Objects.equals(flow, that.flow) &&
                Objects.equals(pcr, that.pcr) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(hashedCustomerIdentifier, that.hashedCustomerIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flow, pcr, amount, hashedCustomerIdentifier);
    }

    @Override
    public String toString() {
        return "SingleConsentRequest{" +
                "flow=" + flow +
                ", pcr=" + pcr +
                ", amount=" + amount +
                ", hashedCustomerIdentifier='" + hashedCustomerIdentifier + '\'' +
                '}';
    }
}
