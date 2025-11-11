package nz.co.blink.debit.dto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Minimal utility class for DTO URL query string methods.
 */
public class ApiClient {

    /**
     * URL encodes a string value.
     *
     * @param value the string to encode
     * @return the URL-encoded string
     */
    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Converts a value to a string for URL query parameters.
     *
     * @param value the value to convert
     * @return the string representation
     */
    public static String valueToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        return String.valueOf(value);
    }
}
