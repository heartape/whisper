package com.heartape.whisper.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeToTimestampSerializer
        extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value,
                          JsonGenerator gen,
                          SerializerProvider serializers)
            throws IOException {

        long epochMilli = value
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        gen.writeNumber(epochMilli);
    }
}
