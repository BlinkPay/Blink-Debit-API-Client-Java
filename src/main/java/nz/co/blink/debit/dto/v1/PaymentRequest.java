/**
 * Copyright (c) 2022 BlinkPay
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
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * The payment request model.
 */
@Schema(description = "The payment request model.")
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-07-22T05:16:54.376Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentRequest {

    @JsonProperty("consent_id")
    private UUID consentId = null;

    @JsonProperty("enduring_payment")
    private EnduringPaymentRequest enduringPayment = null;

    @JsonProperty("account_reference_id")
    private UUID accountReferenceId = null;

    public PaymentRequest consentId(UUID consentId) {
        this.consentId = consentId;
        return this;
    }

    /**
     * The consent ID
     *
     * @return consentId
     **/
    @Schema(required = true, description = "The consent ID")
    @NotNull(message = "Consent ID must not be null")
    @Valid
    public UUID getConsentId() {
        return consentId;
    }

    public void setConsentId(UUID consentId) {
        this.consentId = consentId;
    }

    public PaymentRequest enduringPayment(EnduringPaymentRequest enduringPayment) {
        this.enduringPayment = enduringPayment;
        return this;
    }

    /**
     * Get enduringPayment
     *
     * @return enduringPayment
     **/
    @Schema(description = "")
    @Valid
    public EnduringPaymentRequest getEnduringPayment() {
        return enduringPayment;
    }

    public void setEnduringPayment(EnduringPaymentRequest enduringPayment) {
        this.enduringPayment = enduringPayment;
    }

    public PaymentRequest accountReferenceId(UUID accountReferenceId) {
        this.accountReferenceId = accountReferenceId;
        return this;
    }

    /**
     * The Westpac DFA account reference ID from account list.
     *
     * @return accountReferenceId
     **/
    @Schema(description = "The account reference ID from account list. This is required if the account selection information was provided to you on the consents endpoint.")
    public UUID getAccountReferenceId() {
        return accountReferenceId;
    }

    public void setAccountReferenceId(UUID accountReferenceId) {
        this.accountReferenceId = accountReferenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaymentRequest paymentRequest = (PaymentRequest) o;
        return Objects.equals(this.consentId, paymentRequest.consentId)
                && Objects.equals(this.enduringPayment, paymentRequest.enduringPayment)
                && Objects.equals(this.accountReferenceId, paymentRequest.accountReferenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consentId, enduringPayment, accountReferenceId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PaymentRequest {\n");
        sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
        sb.append("    enduringPayment: ").append(toIndentedString(enduringPayment)).append("\n");
        sb.append("    accountReferenceId: ").append(toIndentedString(accountReferenceId)).append("\n");
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
