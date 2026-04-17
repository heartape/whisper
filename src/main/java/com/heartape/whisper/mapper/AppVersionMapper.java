
package com.heartape.whisper.mapper;

import com.heartape.whisper.entity.AppVersion;
import com.heartape.whisper.common.constant.PlatformEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppVersionMapper {

    /**
     * 根据ID查询版本信息
     */
    AppVersion findById(Long id);

    /**
     * 根据平台查询最新版本
     */
    AppVersion findLatestByPlatform(PlatformEnum platform);

    /**
     * 根据平台查询所有版本
     */
    List<AppVersion> findAllByPlatform(PlatformEnum platform);

    /**
     * 插入新版本
     */
    int insert(AppVersion appVersion);

    /**
     * 更新版本信息
     */
    void updateById(AppVersion appVersion);

    /**
     * 删除版本
     */
    void delete(Long id);
}
