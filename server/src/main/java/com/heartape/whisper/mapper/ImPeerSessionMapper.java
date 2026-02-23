package com.heartape.whisper.mapper;

import com.heartape.whisper.entity.ImPeerSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ImPeerSessionMapper {

    ImPeerSession selectByUsers(@Param("uid1") Long uid1, @Param("uid2") Long uid2);

    int insert(ImPeerSession peerSession);
}
