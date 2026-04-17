
package com.heartape.whisper.service;

import com.heartape.whisper.entity.AppVersion;
import com.heartape.whisper.common.constant.PlatformEnum;

import java.util.List;

public interface AppVersionService {

    /**
     * 根据平台查询最新版本
     */
    AppVersion getLatestByPlatform(PlatformEnum platform);

    /**
     * 根据平台查询所有版本
     */
    List<AppVersion> getAllByPlatform(PlatformEnum platform);

    /**
     * 创建新版本
     */
    void create(AppVersion appVersion);

    /**
     * 更新版本信息
     */
    void publish(Long id);

}
