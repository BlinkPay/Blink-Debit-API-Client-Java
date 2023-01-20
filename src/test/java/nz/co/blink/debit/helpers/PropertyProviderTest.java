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
package nz.co.blink.debit.helpers;

import nz.co.blink.debit.enums.BlinkPayProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test case for {@link PropertyProvider}.
 */
@ExtendWith(SystemStubsExtension.class)
@Tag("unit")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PropertyProviderTest {

    private static final PropertyProvider PROPERTY_PROVIDER = new EnvironmentVariablePropertyProvider(new SystemPropertyProvider(new FilePropertyProvider(new DefaultPropertyProvider())));

    private static final Properties PROPERTIES = new Properties();

    @Test
    @DisplayName("Verify that environment variable is retrieved")
    @Order(1)
    void getEnvironmentVariable() throws Exception {
        EnvironmentVariables environmentVariables = new EnvironmentVariables(BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS.name(), "40");
        environmentVariables.setup();

        assertThat(PROPERTY_PROVIDER.getProperty(PROPERTIES, BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS))
                .isEqualTo("40");

        environmentVariables.teardown();
    }

    @Test
    @DisplayName("Verify that system property is retrieved")
    @Order(2)
    void getSystemProperty() {
        System.setProperty(BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS.getPropertyName(), "30");

        assertThat(PROPERTY_PROVIDER.getProperty(PROPERTIES, BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS))
                .isEqualTo("30");

        System.clearProperty(BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS.getPropertyName());
    }

    @Test
    @DisplayName("Verify that property is retrieved")
    @Order(3)
    void getProperty() {
        PROPERTIES.setProperty(BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS.getPropertyName(), "20");

        assertThat(PROPERTY_PROVIDER.getProperty(PROPERTIES, BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS))
                .isEqualTo("20");

        PROPERTIES.remove(BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS.getPropertyName());
    }

    @Test
    @DisplayName("Verify that default value is retrieved")
    @Order(4)
    void getDefault() {
        assertThat(PROPERTY_PROVIDER.getProperty(PROPERTIES, BlinkPayProperty.BLINKPAY_MAX_CONNECTIONS))
                .isEqualTo("10");
    }
}
