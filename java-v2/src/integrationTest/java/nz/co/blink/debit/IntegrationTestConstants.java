/**
 * Copyright (c) 2025 BlinkPay
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
package nz.co.blink.debit;

import nz.co.blink.debit.dto.v1.Bank;

/**
 * Shared constants for integration tests.
 */
public final class IntegrationTestConstants {

    // URIs
    public static final String REDIRECT_URI = "https://www.blinkpay.co.nz/sample-merchant-return-page";
    public static final String CALLBACK_URL = "https://www.mymerchant.co.nz/callback";

    // Customer
    public static final String CUSTOMER_HASH = "88df3798e32512ac340164f7ed133343d6dcb4888e4a91b03512dedd9800d12e";

    // Bank
    public static final Bank DEFAULT_BANK = Bank.PNZ;

    // Decoupled flow - auto-authorize phone for PNZ bank in staging
    public static final String PHONE_NUMBER = "+64-259531933";

    // PCR
    public static final String PARTICULARS = "particulars";
    public static final String CODE = "code";
    public static final String REFERENCE = "reference";

    private IntegrationTestConstants() {
        // Utility class
    }
}
