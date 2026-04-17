package com.heartape.whisper.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.heartape.whisper.config.UploadProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@AllArgsConstructor
@Component
public class AvatarUrlSerializer extends JsonSerializer<String> {

    private final UploadProperties uploadProperties;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else if (value.startsWith("http")) {
            gen.writeString(value);
        } else {
            gen.writeString(uploadProperties.getServer() + value);
        }
    }
}