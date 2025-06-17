package com.melvstein.ecommerce.api.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class CustomInstantSerializer extends InstantSerializer {
    public static final CustomInstantSerializer INSTANCE = new CustomInstantSerializer();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));

    public CustomInstantSerializer() {
        super(InstantSerializer.INSTANCE, false, false, FORMATTER);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        // Truncate to milliseconds and format with UTC timezone
        Instant truncated = value.truncatedTo(ChronoUnit.MILLIS);
        gen.writeString(FORMATTER.format(truncated));
    }
}