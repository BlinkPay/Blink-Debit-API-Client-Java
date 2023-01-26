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
package nz.co.blink.debit.dto.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * QuickPaymentResponse
 */
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-07-22T00:54:15.842Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuickPaymentResponse {

    @JsonProperty("quick_payment_id")
    private UUID quickPaymentId = null;

    @JsonProperty("consent")
    private Consent consent = null;

    public QuickPaymentResponse quickPaymentId(UUID quickPaymentId) {
        this.quickPaymentId = quickPaymentId;
        return this;
    }

    /**
     * The quick payment ID.
     *
     * @return quickPaymentId
     **/
    @Schema(required = true, description = "The quick payment ID.")
    @NotNull(message = "Quick payment ID must not be null")
    @Valid
    public UUID getQuickPaymentId() {
        return quickPaymentId;
    }

    public void setQuickPaymentId(UUID quickPaymentId) {
        this.quickPaymentId = quickPaymentId;
    }

    public QuickPaymentResponse consent(Consent consent) {
        this.consent = consent;
        return this;
    }

    /**
     * Get consent
     *
     * @return consent
     **/
    @Schema(required = true, description = "")
    @NotNull(message = "Consent must not be null")
    @Valid
    public Consent getConsent() {
        return consent;
    }

    public void setConsent(Consent consent) {
        this.consent = consent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuickPaymentResponse quickPaymentResponse = (QuickPaymentResponse) o;
        return Objects.equals(this.quickPaymentId, quickPaymentResponse.quickPaymentId)
                && Objects.equals(this.consent, quickPaymentResponse.consent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quickPaymentId, consent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QuickPaymentResponse {\n");
        sb.append("    quickPaymentId: ").append(toIndentedString(quickPaymentId)).append("\n");
        sb.append("    consent: ").append(toIndentedString(consent)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
