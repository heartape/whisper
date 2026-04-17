package com.heartape.whisper.common;

import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SqlInjectionDetector {

    private final WallConfig wallConfig = new WallConfig();

    @PostConstruct
    private void init() {
        // 配置允许的 SQL 行为，对于纯文本检查，通常保持默认严格模式
        wallConfig.setSelectAllow(false);
        wallConfig.setDeleteAllow(false);
        wallConfig.setUpdateAllow(false);
        wallConfig.setInsertAllow(false);
        // 禁止非基本语句
        wallConfig.setNoneBaseStatementAllow(false);
    }

    /**
     * 检查文本是否包含 SQL 注入风险
     * 注意：这个检测非常严格，如果用户聊代码发了 "select * from user"，会被判定为注入。
     * 建议：仅作为风控日志记录，或结合业务场景降级处理。
     */
    public boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return WallUtils.isValidateMySql(value, wallConfig);
    }
}
