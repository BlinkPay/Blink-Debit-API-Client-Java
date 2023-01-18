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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The model for the available bank.
 */
@Schema(description = "The model for the available bank.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-12-10T03:00:57.047Z[GMT]")
public class AvailableBank {

    @JsonProperty("bank")
    private Bank bank = null;

    @JsonProperty("available_flows")
    @Valid
    private List<AvailableFlow> availableFlows = new ArrayList<>();

    public AvailableBank bank(Bank bank) {
        this.bank = bank;
        return this;
    }

    /**
     * Get bank
     *
     * @return bank
     **/
    @Schema(required = true, description = "")
    @NotNull
    @Valid
    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public AvailableBank availableFlows(List<AvailableFlow> availableFlows) {
        this.availableFlows = availableFlows;
        return this;
    }

    public AvailableBank addAvailableFlowsItem(AvailableFlow availableFlowsItem) {
        this.availableFlows.add(availableFlowsItem);
        return this;
    }

    /**
     * The available authorization flows for the bank.
     *
     * @return availableFlows
     **/
    @Schema(required = true, description = "The available authorization flows for the bank.")
    @NotNull
    @Valid
    public List<AvailableFlow> getAvailableFlows() {
        return availableFlows;
    }

    public void setAvailableFlows(List<AvailableFlow> availableFlows) {
        this.availableFlows = availableFlows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AvailableBank availableBank = (AvailableBank) o;
        return Objects.equals(this.bank, availableBank.bank)
                && Objects.equals(this.availableFlows, availableBank.availableFlows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bank, availableFlows);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AvailableBank {\n");
        sb.append("    bank: ").append(toIndentedString(bank)).append("\n");
        sb.append("    availableFlows: ").append(toIndentedString(availableFlows)).append("\n");
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
