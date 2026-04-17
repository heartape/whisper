package com.heartape.whisper.controller;

import com.heartape.whisper.common.Result;
import com.heartape.whisper.common.constant.PlatformEnum;
import com.heartape.whisper.entity.AppVersion;
import com.heartape.whisper.entity.result.AppVersionResult;
import com.heartape.whisper.service.AppVersionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/system")
public class SystemController {

    private final AppVersionService appVersionService;

    @GetMapping("/version/latest")
    public Result<AppVersionResult> latestVersion(@RequestParam PlatformEnum platform) {
        final AppVersion appVersion = appVersionService.getLatestByPlatform(platform);
        final AppVersionResult appVersionResult = AppVersionResult.of(appVersion);
        return Result.success(appVersionResult);
    }

}
