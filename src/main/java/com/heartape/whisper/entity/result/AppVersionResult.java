package com.heartape.whisper.entity.result;

import com.heartape.whisper.common.constant.PlatformEnum;
import com.heartape.whisper.common.constant.UpdateStrategyEnum;
import com.heartape.whisper.entity.AppVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MAJOR (主版本号，如 v1 -> v2)：发生不向下兼容的颠覆性修改。例如底层协议从 HTTP 改为 WebSocket，或者老表结构彻底废弃。
 * MINOR (次版本号，如 v1.1 -> v1.2)：增加向下兼容的新功能。例如新增了“群公告”功能，旧版本即使不支持也不会崩溃。
 * PATCH (修订号，如 v1.2.0 -> v1.2.1)：向下兼容的 Bug 修复。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionResult {

    /** 平台枚举: ANDROID, IOS, WINDOWS, MAC */
    private PlatformEnum platform;

    /** 内部单调递增版本号 (如 10) */
    private Integer versionCode;

    /** 展示版本号 (如 "1.2.0") */
    private String versionName;

    /** 最低兼容版本号！低于这个版本的旧 App 将被强制更新 */
    private Integer minCompatibleCode;

    /** 下载地址 / 商店跳转链接 */
    private String downloadUrl;
    private String apkMd5;

    /** 更新日志 */
    private String releaseNotes;

    /** 策略枚举: FORCE(强制), RECOMMEND(推荐/弹窗可关), SILENT(静默/红点提示) */
    private UpdateStrategyEnum updateStrategy;

    public static AppVersionResult of(AppVersion appVersion) {
        return new AppVersionResult(appVersion.getPlatform(), appVersion.getVersionCode(), appVersion.getVersionName(), appVersion.getMinCompatibleCode(), appVersion.getDownloadUrl(), appVersion.getApkMd5(), appVersion.getReleaseNotes(), appVersion.getUpdateStrategy());
    }

}

