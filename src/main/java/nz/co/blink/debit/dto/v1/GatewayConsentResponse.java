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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GatewayConsentResponse
 */
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-12T03:12:35.655Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayConsentResponse {

    @JsonProperty("consent")
    private Consent consent = null;

    @JsonProperty("merchant_name")
    private String merchantName = null;

    @JsonProperty("merchant_account_number")
    private String merchantAccountNumber = null;

    @JsonProperty("quick_payment")
    private Boolean quickPayment = null;

    @JsonProperty("merchant_redirect_uri")
    private String merchantRedirectUri = null;

    @JsonProperty("gateway_redirect_uri")
    private String gatewayRedirectUri = null;

    @JsonProperty("bank_redirect_uri")
    private String bankRedirectUri = null;

    @JsonProperty("available_banks")
    @Valid
    private List<AvailableBank> availableBanks = new ArrayList<>();

    public GatewayConsentResponse consent(Consent consent) {
        this.consent = consent;
        return this;
    }

    /**
     * Get consent
     *
     * @return consent
     **/
    @Schema(description = "")
    @Valid
    public Consent getConsent() {
        return consent;
    }

    public void setConsent(Consent consent) {
        this.consent = consent;
    }

    public GatewayConsentResponse merchantName(String merchantName) {
        this.merchantName = merchantName;
        return this;
    }

    /**
     * The merchant's full name.
     *
     * @return merchantName
     **/
    @Schema(example = "ACME Inc.", description = "The merchant's full name.")
    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public GatewayConsentResponse merchantAccountNumber(String merchantAccountNumber) {
        this.merchantAccountNumber = merchantAccountNumber;
        return this;
    }

    /**
     * The merchant's account number in BB-bbbb-AAAAAAA-SS format.
     *
     * @return merchantAccountNumber
     **/
    @Schema(example = "02-0544-0054496-01", description = "The merchant's account number in BB-bbbb-AAAAAAA-SS format.")
    public String getMerchantAccountNumber() {
        return merchantAccountNumber;
    }

    public void setMerchantAccountNumber(String merchantAccountNumber) {
        this.merchantAccountNumber = merchantAccountNumber;
    }

    public GatewayConsentResponse quickPayment(Boolean quickPayment) {
        this.quickPayment = quickPayment;
        return this;
    }

    /**
     * The flag if the consent is a quick payment or not.
     *
     * @return quickPayment
     **/
    @Schema(description = "The flag if the consent is a quick payment or not.")
    public Boolean isQuickPayment() {
        return quickPayment;
    }

    public void setQuickPayment(Boolean quickPayment) {
        this.quickPayment = quickPayment;
    }

    public GatewayConsentResponse merchantRedirectUri(String merchantRedirectUri) {
        this.merchantRedirectUri = merchantRedirectUri;
        return this;
    }

    /**
     * The redirect URI back to the merchant.
     *
     * @return merchantRedirectUri
     **/
    @Schema(required = true, description = "The redirect URI back to the merchant.")
    @NotNull
    public String getMerchantRedirectUri() {
        return merchantRedirectUri;
    }

    public void setMerchantRedirectUri(String merchantRedirectUri) {
        this.merchantRedirectUri = merchantRedirectUri;
    }

    public GatewayConsentResponse gatewayRedirectUri(String gatewayRedirectUri) {
        this.gatewayRedirectUri = gatewayRedirectUri;
        return this;
    }

    /**
     * The redirect URI that Blink Gateway Redirect flow will return to
     *
     * @return gatewayRedirectUri
     **/
    @Schema(required = true, description = "The redirect URI that Blink Gateway Redirect flow will return to")
    @NotNull
    public String getGatewayRedirectUri() {
        return gatewayRedirectUri;
    }

    public void setGatewayRedirectUri(String gatewayRedirectUri) {
        this.gatewayRedirectUri = gatewayRedirectUri;
    }

    public GatewayConsentResponse bankRedirectUri(String bankRedirectUri) {
        this.bankRedirectUri = bankRedirectUri;
        return this;
    }

    /**
     * The Redirect URI that the customer will go to in order to authenticate with their bank, if applicable (i.e. during Redirect Flow after the bank and flow selection).
     *
     * @return bankRedirectUri
     **/
    @Schema(required = true, description = "The Redirect URI that the customer will go to in order to authenticate with their bank, if applicable (i.e. during Redirect Flow after the bank and flow selection).")
    @NotNull
    public String getBankRedirectUri() {
        return bankRedirectUri;
    }

    public void setBankRedirectUri(String bankRedirectUri) {
        this.bankRedirectUri = bankRedirectUri;
    }

    public GatewayConsentResponse availableBanks(List<AvailableBank> availableBanks) {
        this.availableBanks = availableBanks;
        return this;
    }

    public GatewayConsentResponse addAvailableBanksItem(AvailableBank availableBanksItem) {
        this.availableBanks.add(availableBanksItem);
        return this;
    }

    /**
     * The merchant's supported banks.
     *
     * @return availableBanks
     **/
    @Schema(required = true, description = "The merchant's supported banks.")
    @NotNull
    @Valid
    public List<AvailableBank> getAvailableBanks() {
        return availableBanks;
    }

    public void setAvailableBanks(List<AvailableBank> availableBanks) {
        this.availableBanks = availableBanks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GatewayConsentResponse gatewayConsentResponse = (GatewayConsentResponse) o;
        return Objects.equals(this.consent, gatewayConsentResponse.consent)
                && Objects.equals(this.merchantName, gatewayConsentResponse.merchantName)
                && Objects.equals(this.merchantAccountNumber, gatewayConsentResponse.merchantAccountNumber)
                && Objects.equals(this.quickPayment, gatewayConsentResponse.quickPayment)
                && Objects.equals(this.merchantRedirectUri, gatewayConsentResponse.merchantRedirectUri)
                && Objects.equals(this.gatewayRedirectUri, gatewayConsentResponse.gatewayRedirectUri)
                && Objects.equals(this.bankRedirectUri, gatewayConsentResponse.bankRedirectUri)
                && Objects.equals(this.availableBanks, gatewayConsentResponse.availableBanks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consent, merchantName, merchantAccountNumber, quickPayment, merchantRedirectUri,
                gatewayRedirectUri, bankRedirectUri, availableBanks);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GatewayConsentResponse {\n");
        sb.append("    consent: ").append(toIndentedString(consent)).append("\n");
        sb.append("    merchantName: ").append(toIndentedString(merchantName)).append("\n");
        sb.append("    merchantAccountNumber: ").append(toIndentedString(merchantAccountNumber)).append("\n");
        sb.append("    quickPayment: ").append(toIndentedString(quickPayment)).append("\n");
        sb.append("    merchantRedirectUri: ").append(toIndentedString(merchantRedirectUri)).append("\n");
        sb.append("    gatewayRedirectUri: ").append(toIndentedString(gatewayRedirectUri)).append("\n");
        sb.append("    bankRedirectUri: ").append(toIndentedString(bankRedirectUri)).append("\n");
        sb.append("    availableBanks: ").append(toIndentedString(availableBanks)).append("\n");
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
