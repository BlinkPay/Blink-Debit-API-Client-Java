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
import java.net.URI;
import java.util.Objects;

/**
 * The PCR and amount to use in the partial_refund request.
 * <p>
 * NOTE: This is a hand-written class to avoid circular reference issues.
 */
@JsonTypeName("partial_refund")
public class PartialRefundRequest extends RefundDetail {

    @NotNull
    @Valid
    @JsonProperty("amount")
    private Amount amount;

    @NotNull
    @Valid
    @JsonProperty("pcr")
    private Pcr pcr;

    @JsonProperty("consent_redirect")
    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    private URI consentRedirect;

    public PartialRefundRequest() {
        super();
    }

    public PartialRefundRequest amount(Amount amount) {
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

    public PartialRefundRequest pcr(Pcr pcr) {
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

    public PartialRefundRequest consentRedirect(URI consentRedirect) {
        this.consentRedirect = consentRedirect;
        return this;
    }

    /**
     * The URI to redirect back to once the consent is completed (if applicable).
     *
     * @return consentRedirect
     */
    public URI getConsentRedirect() {
        return consentRedirect;
    }

    public void setConsentRedirect(URI consentRedirect) {
        this.consentRedirect = consentRedirect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartialRefundRequest that = (PartialRefundRequest) o;
        return Objects.equals(paymentId, that.paymentId) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(pcr, that.pcr) &&
                Objects.equals(consentRedirect, that.consentRedirect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, amount, pcr, consentRedirect);
    }

    @Override
    public String toString() {
        return "PartialRefundRequest{" +
                "paymentId=" + paymentId +
                ", amount=" + amount +
                ", pcr=" + pcr +
                ", consentRedirect=" + consentRedirect +
                '}';
    }
}
