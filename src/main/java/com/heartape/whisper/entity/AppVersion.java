
package com.heartape.whisper.entity;

import com.heartape.whisper.common.constant.PlatformEnum;
import com.heartape.whisper.common.constant.UpdateStrategyEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppVersion {

    private Long id;

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

    /** 是否已发布 */
    private Boolean isPublished;

    /** 发布时间 */
    private LocalDateTime publishTime;
}
