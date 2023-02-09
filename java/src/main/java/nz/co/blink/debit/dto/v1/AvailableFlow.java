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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.annotation.processing.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The model for the available flow.
 */
@Schema(description = "The model for the available flow.")
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-12-10T03:00:57.047Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableFlow {

    /**
     * The authorization flow.
     */
    public enum FlowEnum {
        REDIRECT("redirect"),

        DECOUPLED("decoupled");

        private String value;

        FlowEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static FlowEnum fromValue(String text) {
            for (FlowEnum b : FlowEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("flow")
    private FlowEnum flow = null;

    @JsonProperty("available_identifiers")
    @Valid
    private List<AvailableIdentifier> availableIdentifiers = null;

    public AvailableFlow flow(FlowEnum flow) {
        this.flow = flow;
        return this;
    }

    /**
     * The authorization flow.
     *
     * @return flow
     **/
    @Schema(example = "redirect", required = true, description = "The authorization flow.")
    @NotNull
    public FlowEnum getFlow() {
        return flow;
    }

    public void setFlow(FlowEnum flow) {
        this.flow = flow;
    }

    public AvailableFlow availableIdentifiers(List<AvailableIdentifier> availableIdentifiers) {
        this.availableIdentifiers = availableIdentifiers;
        return this;
    }

    public AvailableFlow addAvailableIdentifiersItem(AvailableIdentifier availableIdentifiersItem) {
        if (this.availableIdentifiers == null) {
            this.availableIdentifiers = new ArrayList<AvailableIdentifier>();
        }
        this.availableIdentifiers.add(availableIdentifiersItem);
        return this;
    }

    /**
     * If decoupled flow is available, this will show the available fields to use to identify the customer with their bank.
     *
     * @return availableIdentifiers
     **/
    @Schema(description = "If decoupled flow is available, this will show the available fields to use to identify the customer with their bank.")
    @Valid
    public List<AvailableIdentifier> getAvailableIdentifiers() {
        return availableIdentifiers;
    }

    public void setAvailableIdentifiers(List<AvailableIdentifier> availableIdentifiers) {
        this.availableIdentifiers = availableIdentifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AvailableFlow availableFlow = (AvailableFlow) o;
        return Objects.equals(this.flow, availableFlow.flow)
                && Objects.equals(this.availableIdentifiers, availableFlow.availableIdentifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flow, availableIdentifiers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AvailableFlow {\n");
        sb.append("    flow: ").append(toIndentedString(flow)).append("\n");
        sb.append("    availableIdentifiers: ").append(toIndentedString(availableIdentifiers)).append("\n");
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
