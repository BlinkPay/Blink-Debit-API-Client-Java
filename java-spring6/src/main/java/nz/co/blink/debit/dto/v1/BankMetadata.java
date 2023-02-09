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
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Information about a banks enabled features.
 */
@Schema(description = "Information about a banks enabled features.")
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-04-22T17:59:56.975143+12:00[Pacific/Auckland]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankMetadata {

    @JsonProperty("name")
    private Bank name = null;

    @JsonProperty("features")
    private BankmetadataFeatures features = null;

    @JsonProperty("redirect_flow")
    private BankmetadataRedirectFlow redirectFlow = null;

    public BankMetadata name(Bank name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @Schema(required = true, description = "")
    @NotNull(message = "Bank must not be null")
    @Valid
    public Bank getName() {
        return name;
    }

    public void setName(Bank name) {
        this.name = name;
    }

    public BankMetadata features(BankmetadataFeatures features) {
        this.features = features;
        return this;
    }

    /**
     * Get features
     *
     * @return features
     **/
    @Schema(required = true, description = "")
    @NotNull(message = "Features must not be null")
    @Valid
    public BankmetadataFeatures getFeatures() {
        return features;
    }

    public void setFeatures(BankmetadataFeatures features) {
        this.features = features;
    }

    public BankMetadata redirectFlow(BankmetadataRedirectFlow redirectFlow) {
        this.redirectFlow = redirectFlow;
        return this;
    }

    /**
     * Get redirectFlow
     *
     * @return redirectFlow
     **/
    @Schema(required = true, description = "")
    @NotNull(message = "Redirect flow must not be null")
    @Valid
    public BankmetadataRedirectFlow getRedirectFlow() {
        return redirectFlow;
    }

    public void setRedirectFlow(BankmetadataRedirectFlow redirectFlow) {
        this.redirectFlow = redirectFlow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BankMetadata bankMetadata = (BankMetadata) o;
        return Objects.equals(this.name, bankMetadata.name)
                && Objects.equals(this.features, bankMetadata.features)
                && Objects.equals(this.redirectFlow, bankMetadata.redirectFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, features, redirectFlow);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BankMetadata {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    features: ").append(toIndentedString(features)).append("\n");
        sb.append("    redirectFlow: ").append(toIndentedString(redirectFlow)).append("\n");
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
