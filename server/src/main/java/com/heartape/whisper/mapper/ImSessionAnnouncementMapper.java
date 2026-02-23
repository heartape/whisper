package com.heartape.whisper.mapper;

import com.heartape.whisper.entity.ImSessionAnnouncement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImSessionAnnouncementMapper {
    int insertOrUpdate(ImSessionAnnouncement imSessionAnnouncement);

    ImSessionAnnouncement selectBySessionId(Long sessionId);
}
