package nz.co.blink.debit.helpers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The custom {@link OffsetDateTime} {@link JsonSerializer}.
 *
 * @author Rey Vincent Babilonia
 */
public class CustomOffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

    @Override
    public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
