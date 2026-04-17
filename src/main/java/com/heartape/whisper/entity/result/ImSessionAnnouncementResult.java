package com.heartape.whisper.entity.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heartape.whisper.entity.ImSessionAnnouncement;
import com.heartape.whisper.json.serializer.LocalDateTimeToTimestampSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImSessionAnnouncementResult {
    private Long userId;
    private String content;
    @JsonSerialize(using = LocalDateTimeToTimestampSerializer.class)
    private LocalDateTime publishTime;

    public static ImSessionAnnouncementResult of(ImSessionAnnouncement imSessionAnnouncement) {
        return new ImSessionAnnouncementResult(imSessionAnnouncement.getUserId(), imSessionAnnouncement.getContent(), imSessionAnnouncement.getPublishTime());
    }
}
