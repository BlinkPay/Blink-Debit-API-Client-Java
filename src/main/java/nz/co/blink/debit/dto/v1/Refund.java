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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import nz.co.blink.debit.helpers.CustomOffsetDateTimeDeserializer;
import nz.co.blink.debit.helpers.CustomOffsetDateTimeSerializer;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * The model for a refund.
 */
@Schema(description = "The model for a refund.")
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-02-13T21:29:12.057Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Refund {

    @JsonProperty("refund_id")
    private UUID refundId = null;

    /**
     * Gets or Sets status
     */
    public enum StatusEnum {

        FAILED("failed"),

        PROCESSING("processing"),

        COMPLETED("completed");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StatusEnum fromValue(String status) {
            return Arrays.stream(StatusEnum.values())
                    .filter(statusEnum -> String.valueOf(statusEnum.value).equals(status))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + status));
        }
    }

    @JsonProperty("status")
    private StatusEnum status = null;

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomOffsetDateTimeSerializer.class)
    @JsonDeserialize(using = CustomOffsetDateTimeDeserializer.class)
    private OffsetDateTime creationTimestamp = null;

    @JsonProperty("status_updated_timestamp")
    @JsonSerialize(using = CustomOffsetDateTimeSerializer.class)
    @JsonDeserialize(using = CustomOffsetDateTimeDeserializer.class)
    private OffsetDateTime statusUpdatedTimestamp = null;

    @JsonProperty("account_number")
    private String accountNumber = null;

    @JsonProperty("detail")
    private OneOfrefundRequest detail = null;

    public Refund refundId(UUID refundId) {
        this.refundId = refundId;
        return this;
    }

    /**
     * The refund ID.
     *
     * @return refundId
     **/
    @Schema(required = true, description = "The refund ID.")
    @NotNull
    @Valid
    public UUID getRefundId() {
        return refundId;
    }

    public void setRefundId(UUID refundId) {
        this.refundId = refundId;
    }

    public Refund status(StatusEnum status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @Schema(required = true, description = "")
    @NotNull
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public Refund creationTimestamp(OffsetDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
        return this;
    }

    /**
     * The time that the refund was created.
     *
     * @return creationTimestamp
     **/
    @Schema(required = true, description = "The time that the refund was created.")
    @NotNull
    @Valid
    public OffsetDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(OffsetDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Refund statusUpdatedTimestamp(OffsetDateTime statusUpdatedTimestamp) {
        this.statusUpdatedTimestamp = statusUpdatedTimestamp;
        return this;
    }

    /**
     * The time that the status was last updated.
     *
     * @return statusUpdatedTimestamp
     **/
    @Schema(required = true, description = "The time that the status was last updated.")
    @NotNull
    @Valid
    public OffsetDateTime getStatusUpdatedTimestamp() {
        return statusUpdatedTimestamp;
    }

    public void setStatusUpdatedTimestamp(OffsetDateTime statusUpdatedTimestamp) {
        this.statusUpdatedTimestamp = statusUpdatedTimestamp;
    }

    public Refund accountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    /**
     * The customer account number used or to be used for the refund.
     *
     * @return accountNumber
     **/
    @Schema(example = "03-0296-0481201-00", required = true, description = "The customer account number used or to be used for the refund.")
    @NotNull
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Refund detail(OneOfrefundRequest detail) {
        this.detail = detail;
        return this;
    }

    /**
     * Get detail
     *
     * @return detail
     **/
    @Schema(required = true, description = "")
    @NotNull
    @Valid
    public OneOfrefundRequest getDetail() {
        return detail;
    }

    public void setDetail(OneOfrefundRequest detail) {
        this.detail = detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Refund refund = (Refund) o;
        return Objects.equals(this.refundId, refund.refundId)
                && Objects.equals(this.status, refund.status)
                && Objects.equals(this.creationTimestamp, refund.creationTimestamp)
                && Objects.equals(this.statusUpdatedTimestamp, refund.statusUpdatedTimestamp)
                && Objects.equals(this.accountNumber, refund.accountNumber)
                && Objects.equals(this.detail, refund.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refundId, status, creationTimestamp, statusUpdatedTimestamp, accountNumber, detail);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Refund {\n");
        sb.append("    refundId: ").append(toIndentedString(refundId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    creationTimestamp: ").append(toIndentedString(creationTimestamp)).append("\n");
        sb.append("    statusUpdatedTimestamp: ").append(toIndentedString(statusUpdatedTimestamp)).append("\n");
        sb.append("    accountNumber: ").append(toIndentedString(accountNumber)).append("\n");
        sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
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
