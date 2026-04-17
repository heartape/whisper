
package com.heartape.whisper.service.impl;

import com.heartape.whisper.common.constant.PlatformEnum;
import com.heartape.whisper.entity.AppVersion;
import com.heartape.whisper.exception.BusinessException;
import com.heartape.whisper.mapper.AppVersionMapper;
import com.heartape.whisper.service.AppVersionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@AllArgsConstructor
@Service
public class AppVersionServiceImpl implements AppVersionService {

    private final AppVersionMapper appVersionMapper;

    @Override
    public AppVersion getLatestByPlatform(PlatformEnum platform) {
        if (platform == null) {
            throw new BusinessException("平台类型不能为空");
        }
        final AppVersion appVersion = appVersionMapper.findLatestByPlatform(platform);
        if (appVersion == null) {
            throw new BusinessException("未找到最新版本");
        }
        return appVersion;
    }

    @Override
    public List<AppVersion> getAllByPlatform(PlatformEnum platform) {
        if (platform == null) {
            throw new BusinessException("平台类型不能为空");
        }
        return appVersionMapper.findAllByPlatform(platform);
    }

    @Override
    public void create(AppVersion appVersion) {
        if (appVersion == null) {
            throw new BusinessException("版本信息不能为空");
        }
        if (appVersion.getPlatform() == null) {
            throw new BusinessException("平台类型不能为空");
        }
        if (appVersion.getVersionCode() == null) {
            throw new BusinessException("版本号不能为空");
        }
        if (!StringUtils.hasText(appVersion.getVersionName())) {
            throw new BusinessException("版本名称不能为空");
        }
        if (appVersion.getMinCompatibleCode() == null) {
            throw new BusinessException("最低兼容版本号不能为空");
        }
        if (!StringUtils.hasText(appVersion.getDownloadUrl())) {
            throw new BusinessException("下载地址不能为空");
        }
        if (appVersion.getUpdateStrategy() == null) {
            throw new BusinessException("更新策略不能为空");
        }
        if (appVersion.getIsPublished() == null) {
            appVersion.setIsPublished(false);
        }

        appVersionMapper.insert(appVersion);
    }

    @Override
    public void publish(Long id) {
        if (id == null) {
            throw new BusinessException("版本ID不能为空");
        }
        final AppVersion appVersion = new AppVersion();
        appVersion.setIsPublished(true);
        appVersionMapper.updateById(appVersion);
    }

}
