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
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * BankmetadataFeaturesEnduringConsent
 */
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-04-22T17:59:56.975143+12:00[Pacific/Auckland]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankmetadataFeaturesEnduringConsent {

    @JsonProperty("enabled")
    private Boolean enabled = null;

    @JsonProperty("maximum_consent")
    private String maximumConsent = null;

    @JsonProperty("consent_indefinite")
    private Boolean consentIndefinite = null;

    public BankmetadataFeaturesEnduringConsent enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * If enduring consent is disabled, only single payment consents can be issued.
     *
     * @return enabled
     **/
    @Schema(required = true, description = "If enduring consent is disabled, only single payment consents can be issued.")
    @NotNull(message = "Enabled flag must not be null")
    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public BankmetadataFeaturesEnduringConsent maximumConsent(String maximumConsent) {
        this.maximumConsent = maximumConsent;
        return this;
    }

    /**
     * ISO8601 time duration for the maximum allowed enduring consent period, i.e. how long the consent could be used to execute payments for. Appears only if consent_indefinite is false
     *
     * @return maximumConsent
     **/
    @Schema(example = "P12M0DT00H00M00S", description = "ISO8601 time duration for the maximum allowed enduring consent period, i.e. how long the consent could be used to execute payments for. Appears only if consent_indefinite is false")
    public String getMaximumConsent() {
        return maximumConsent;
    }

    public void setMaximumConsent(String maximumConsent) {
        this.maximumConsent = maximumConsent;
    }

    public BankmetadataFeaturesEnduringConsent consentIndefinite(Boolean consentIndefinite) {
        this.consentIndefinite = consentIndefinite;
        return this;
    }

    /**
     * If the conseting period for payments is indefinate or time-limited by the bank
     *
     * @return consentIndefinite
     **/
    @Schema(description = "If the conseting period for payments is indefinate or time-limited by the bank")
    public Boolean isConsentIndefinite() {
        return consentIndefinite;
    }

    public void setConsentIndefinite(Boolean consentIndefinite) {
        this.consentIndefinite = consentIndefinite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BankmetadataFeaturesEnduringConsent bankmetadataFeaturesEnduringConsent = (BankmetadataFeaturesEnduringConsent) o;
        return Objects.equals(this.enabled, bankmetadataFeaturesEnduringConsent.enabled)
                && Objects.equals(this.maximumConsent, bankmetadataFeaturesEnduringConsent.maximumConsent)
                && Objects.equals(this.consentIndefinite, bankmetadataFeaturesEnduringConsent.consentIndefinite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, maximumConsent, consentIndefinite);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BankmetadataFeaturesEnduringConsent {\n");
        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
        sb.append("    maximumConsent: ").append(toIndentedString(maximumConsent)).append("\n");
        sb.append("    consentIndefinite: ").append(toIndentedString(consentIndefinite)).append("\n");
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
