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
import nz.co.blink.debit.exception.BlinkInvalidValueException;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;

/**
 * GatewayConsentRequest
 */
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-07-07T22:26:50.920Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayConsentRequest {

    @JsonProperty("bank")
    private Bank bank = null;

    /**
     * The authorization flow i.e. redirect or decoupled
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
        public static FlowEnum fromValue(String flow) throws BlinkInvalidValueException {
            return Arrays.stream(FlowEnum.values())
                    .filter(flowEnum -> String.valueOf(flowEnum.value).equals(flow))
                    .findFirst()
                    .orElseThrow(() -> new BlinkInvalidValueException("Unknown flow: " + flow));
        }
    }

    @JsonProperty("flow")
    private FlowEnum flow = null;

    @JsonProperty("identifier_type")
    private IdentifierType identifierType = null;

    @JsonProperty("identifier_value")
    private String identifierValue = null;

    @JsonProperty("westpac_account_ref")
    private String westpacAccountRef = null;

    public GatewayConsentRequest bank(Bank bank) {
        this.bank = bank;
        return this;
    }

    /**
     * Get bank
     *
     * @return bank
     **/
    @Schema(required = true, description = "")
    @NotNull(message = "Bank must not be null")
    @Valid
    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public GatewayConsentRequest flow(FlowEnum flow) {
        this.flow = flow;
        return this;
    }

    /**
     * The authorization flow i.e. redirect or decoupled
     *
     * @return flow
     **/
    @Schema(example = "redirect", required = true, description = "The authorization flow i.e. redirect or decoupled")
    @NotNull(message = "Flow must not be null")
    public FlowEnum getFlow() {
        return flow;
    }

    public void setFlow(FlowEnum flow) {
        this.flow = flow;
    }

    public GatewayConsentRequest identifierType(IdentifierType identifierType) {
        this.identifierType = identifierType;
        return this;
    }

    /**
     * Get identifierType
     *
     * @return identifierType
     **/
    @Schema(description = "")
    @Valid
    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(IdentifierType identifierType) {
        this.identifierType = identifierType;
    }

    public GatewayConsentRequest identifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
        return this;
    }

    /**
     * Get identifierValue
     *
     * @return identifierValue
     **/
    @Schema(description = "")
    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public GatewayConsentRequest westpacAccountRef(String westpacAccountRef) {
        this.westpacAccountRef = westpacAccountRef;
        return this;
    }

    /**
     * If account selection is required, this is the index of the selected account.
     *
     * @return westpacAccountRef
     **/
    @Schema(description = "If account selection is required, this is the index of the selected account.")
    public String getWestpacAccountRef() {
        return westpacAccountRef;
    }

    public void setWestpacAccountRef(String westpacAccountRef) {
        this.westpacAccountRef = westpacAccountRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GatewayConsentRequest gatewayConsentRequest = (GatewayConsentRequest) o;
        return Objects.equals(this.bank, gatewayConsentRequest.bank)
                && Objects.equals(this.flow, gatewayConsentRequest.flow)
                && Objects.equals(this.identifierType, gatewayConsentRequest.identifierType)
                && Objects.equals(this.identifierValue, gatewayConsentRequest.identifierValue)
                && Objects.equals(this.westpacAccountRef, gatewayConsentRequest.westpacAccountRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bank, flow, identifierType, identifierValue, westpacAccountRef);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GatewayConsentRequest {\n");
        sb.append("    bank: ").append(toIndentedString(bank)).append("\n");
        sb.append("    flow: ").append(toIndentedString(flow)).append("\n");
        sb.append("    identifierType: ").append(toIndentedString(identifierType)).append("\n");
        sb.append("    identifierValue: ").append(toIndentedString(identifierValue)).append("\n");
        sb.append("    westpacAccountRef: ").append(toIndentedString(westpacAccountRef)).append("\n");
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
