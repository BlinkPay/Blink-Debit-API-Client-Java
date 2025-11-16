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
 * The details for a Gateway flow.
 * <p>
 * NOTE: This is a hand-written class to avoid circular reference issues.
 */
@JsonTypeName("gateway")
public class GatewayFlow extends AuthFlowDetail {

    @NotNull
    @Valid
    @JsonProperty("redirect_uri")
    private URI redirectUri;

    @JsonProperty("redirect_to_app")
    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    private Boolean redirectToApp = false;

    @Valid
    @JsonProperty("flow_hint")
    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    private GatewayFlowAllOfFlowHint flowHint;

    public GatewayFlow() {
    }

    public GatewayFlow redirectUri(URI redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    /**
     * The URL to redirect back to once the payment is completed through the gateway.
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

    public GatewayFlow redirectToApp(Boolean redirectToApp) {
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

    public GatewayFlow flowHint(GatewayFlowAllOfFlowHint flowHint) {
        this.flowHint = flowHint;
        return this;
    }

    /**
     * Get flowHint
     *
     * @return flowHint
     */
    public GatewayFlowAllOfFlowHint getFlowHint() {
        return flowHint;
    }

    public void setFlowHint(GatewayFlowAllOfFlowHint flowHint) {
        this.flowHint = flowHint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatewayFlow that = (GatewayFlow) o;
        return Objects.equals(redirectUri, that.redirectUri) &&
                Objects.equals(redirectToApp, that.redirectToApp) &&
                Objects.equals(flowHint, that.flowHint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redirectUri, redirectToApp, flowHint);
    }

    @Override
    public String toString() {
        return "GatewayFlow{" +
                "redirectUri=" + redirectUri +
                ", redirectToApp=" + redirectToApp +
                ", flowHint=" + flowHint +
                '}';
    }
}
