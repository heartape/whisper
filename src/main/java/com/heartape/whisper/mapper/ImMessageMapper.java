package com.heartape.whisper.mapper;

import com.heartape.whisper.entity.ImMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImMessageMapper {

    int insert(ImMessage message);

    ImMessage selectById(Long id);

}

