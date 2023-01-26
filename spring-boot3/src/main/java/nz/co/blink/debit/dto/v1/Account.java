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
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.UUID;

/**
 * The model for Westpac account.
 */
@Schema(description = "The model for Westpac account.")
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-01T00:24:10.855Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account {

    @JsonProperty("account_reference_id")
    private UUID accountReferenceId = null;

    @JsonProperty("account_number")
    private String accountNumber = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("available_balance")
    private String availableBalance = null;

    public Account accountReferenceId(UUID accountReferenceId) {
        this.accountReferenceId = accountReferenceId;
        return this;
    }

    /**
     * The account reference ID from account list. This is required if the account selection information was provided to you on the consents endpoint.
     *
     * @return accountReferenceId
     **/
    @Schema(example = "a879865c-9f99-4435-a120-ef6bbbe46976", accessMode = Schema.AccessMode.READ_ONLY, description = "The account reference ID from account list. This is required if the account selection information was provided to you on the consents endpoint.")
    public UUID getAccountReferenceId() {
        return accountReferenceId;
    }

    public void setAccountReferenceId(UUID accountReferenceId) {
        this.accountReferenceId = accountReferenceId;
    }

    public Account accountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    /**
     * The account number.
     *
     * @return accountNumber
     **/
    @Schema(example = "03-0296-0481201-00", accessMode = Schema.AccessMode.READ_ONLY, description = "The account number.")
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Account name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The account name.
     *
     * @return name
     **/
    @Schema(example = "Westpac Everyday", accessMode = Schema.AccessMode.READ_ONLY, description = "The account name.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Account availableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
        return this;
    }

    /**
     * The available balance.
     *
     * @return availableBalance
     **/
    @Schema(example = "1000.00", accessMode = Schema.AccessMode.READ_ONLY, description = "The available balance.")
    @Pattern(regexp = "^\\d{1,13}\\.\\d{1,2}$")
    public String getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return Objects.equals(this.accountReferenceId, account.accountReferenceId)
                && Objects.equals(this.accountNumber, account.accountNumber)
                && Objects.equals(this.name, account.name)
                && Objects.equals(this.availableBalance, account.availableBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountReferenceId, accountNumber, name, availableBalance);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Account {\n");
        sb.append("    accountReferenceId: ").append(toIndentedString(accountReferenceId)).append("\n");
        sb.append("    accountNumber: ").append(toIndentedString(accountNumber)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    availableBalance: ").append(toIndentedString(availableBalance)).append("\n");
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
