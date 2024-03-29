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
import com.fasterxml.jackson.annotation.JsonValue;
import nz.co.blink.debit.exception.BlinkInvalidValueException;

import java.util.Arrays;

/**
 * The available periods selection.  A \"monthly\" period with a from_timestamp 2019-08-21T00:00:00 will have periods defined as - 2019-08-21T00:00:00 to 2019-09-20T23:59:59  - 2019-09-21T00:00:00 to 2019-10-20T23:59:59  - 2019-10-21T00:00:00 to 2019-11-20T23:59:59  - Etc  A \"weekly\" period with a from_timestamp 2019-08-21T00:00:00 will have periods defined as  - 2019-08-21T00:00:00 to 2019-08-27T23:59:59  - 2019-08-28T00:00:00 to 2019-09-03T23:59:59  - 2019-09-04T00:00:00 to 2019-09-10T23:59:59  - Etc
 */
public enum Period {

    ANNUAL("annual"),

    DAILY("daily"),

    FORTNIGHTLY("fortnightly"),

    MONTHLY("monthly"),

    WEEKLY("weekly");

    private final String value;

    Period(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Period fromValue(String text) throws BlinkInvalidValueException {
        return Arrays.stream(Period.values())
                .filter(period -> String.valueOf(period.value).equals(text))
                .findFirst()
                .orElseThrow(() -> new BlinkInvalidValueException("Unknown period: " + text));
    }
}
