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
import org.springframework.validation.annotation.Validated;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * AvailableIdentifier
 */
@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-18T01:05:55.507Z[GMT]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableIdentifier {

    @JsonProperty("type")
    private IdentifierType type = null;

    @JsonProperty("regex")
    private String regex = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    public AvailableIdentifier type(IdentifierType type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    @Schema(required = true, description = "")
    @NotNull(message = "Identifier type must not be null")
    @Valid
    public IdentifierType getType() {
        return type;
    }

    public void setType(IdentifierType type) {
        this.type = type;
    }

    public AvailableIdentifier regex(String regex) {
        this.regex = regex;
        return this;
    }

    /**
     * A regex that can be used for validation of the field
     *
     * @return regex
     **/
    @Schema(example = "^[0-9]{9}$", description = "A regex that can be used for validation of the field")
    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public AvailableIdentifier name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The common name of the field
     *
     * @return name
     **/
    @Schema(example = "Access Number", required = true, description = "The common name of the field")
    @NotNull(message = "Name must not be null")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AvailableIdentifier description(String description) {
        this.description = description;
        return this;
    }

    /**
     * The description of the field and/or where to find it
     *
     * @return description
     **/
    @Schema(example = "The nine-digit access number used to login to your internet banking", description = "The description of the field and/or where to find it")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AvailableIdentifier availableIdentifier = (AvailableIdentifier) o;
        return Objects.equals(this.type, availableIdentifier.type)
                && Objects.equals(this.regex, availableIdentifier.regex)
                && Objects.equals(this.name, availableIdentifier.name)
                && Objects.equals(this.description, availableIdentifier.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, regex, name, description);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AvailableIdentifier {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    regex: ").append(toIndentedString(regex)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
