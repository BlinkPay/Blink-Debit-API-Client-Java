package nz.co.blink.debit.helpers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The custom {@link OffsetDateTime} {@link JsonDeserializer}.
 *
 * @author Rey Vincent Babilonia
 */
public class CustomOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String date = jsonParser.getText();

        if (date.contains("+") && !date.endsWith("Z")) {
            String dateTime = date.substring(0, date.indexOf("+"));
            String offset = date.substring(date.indexOf("+")).replace(".", ":");
            String dateTimeOffset = dateTime + offset;
            return OffsetDateTime.parse(dateTimeOffset, DATE_TIME_FORMATTER);
        }

        return OffsetDateTime.parse(date, DATE_TIME_FORMATTER);
    }
}
