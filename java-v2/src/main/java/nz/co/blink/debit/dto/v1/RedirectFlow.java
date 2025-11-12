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
 * The details for a Redirect flow.
 * <p>
 * NOTE: This is a hand-written class to avoid circular reference issues.
 */
@JsonTypeName("redirect")
public class RedirectFlow extends AuthFlowDetail {

    @NotNull
    @Valid
    @JsonProperty("redirect_uri")
    private URI redirectUri;

    @NotNull
    @Valid
    @JsonProperty("bank")
    private Bank bank;

    @JsonProperty("redirect_to_app")
    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    private Boolean redirectToApp = false;

    public RedirectFlow() {
    }

    public RedirectFlow redirectUri(URI redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    /**
     * The URI to redirect back to once the consent is completed.
     * App-based workflows may use deep/universal links.
     * The cid (Consent ID) will be added as a URL parameter.
     * If there is an error, an error parameter will be appended also.
     *
     * @return redirectUri
     */
    public URI getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(URI redirectUri) {
        this.redirectUri = redirectUri;
    }

    public RedirectFlow bank(Bank bank) {
        this.bank = bank;
        return this;
    }

    /**
     * Get bank
     *
     * @return bank
     */
    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public RedirectFlow redirectToApp(Boolean redirectToApp) {
        this.redirectToApp = redirectToApp;
        return this;
    }

    /**
     * Whether the redirect URI goes back to an app directly.
     *
     * @return redirectToApp
     */
    public Boolean getRedirectToApp() {
        return redirectToApp;
    }

    public void setRedirectToApp(Boolean redirectToApp) {
        this.redirectToApp = redirectToApp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedirectFlow that = (RedirectFlow) o;
        return Objects.equals(redirectUri, that.redirectUri) &&
                Objects.equals(bank, that.bank) &&
                Objects.equals(redirectToApp, that.redirectToApp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redirectUri, bank, redirectToApp);
    }

    @Override
    public String toString() {
        return "RedirectFlow{" +
                "redirectUri=" + redirectUri +
                ", bank=" + bank +
                ", redirectToApp=" + redirectToApp +
                '}';
    }
}
