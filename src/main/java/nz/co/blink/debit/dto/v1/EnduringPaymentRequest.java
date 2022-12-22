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

/**
 * The model for an enduring payment request.
 */
@Schema(description = "The model for an enduring payment request.")
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-07-22T00:54:15.842Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnduringPaymentRequest {

    @JsonProperty("pcr")
    private Pcr pcr = null;

    @JsonProperty("amount")
    private Amount amount = null;

    public EnduringPaymentRequest pcr(Pcr pcr) {
        this.pcr = pcr;
        return this;
    }

    /**
     * Get pcr
     *
     * @return pcr
     **/
    @Schema(required = true, description = "")
    @NotNull(message = "PCR must not be null")
    @Valid
    public Pcr getPcr() {
        return pcr;
    }

    public void setPcr(Pcr pcr) {
        this.pcr = pcr;
    }

    public EnduringPaymentRequest amount(Amount amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Get amount
     *
     * @return amount
     **/
    @Schema(required = true, description = "")
    @NotNull(message = "Amount must not be null")
    @Valid
    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EnduringPaymentRequest enduringPaymentRequest = (EnduringPaymentRequest) o;
        return Objects.equals(this.pcr, enduringPaymentRequest.pcr)
                && Objects.equals(this.amount, enduringPaymentRequest.amount)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pcr, amount, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EnduringPaymentRequest {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    pcr: ").append(toIndentedString(pcr)).append("\n");
        sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
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
