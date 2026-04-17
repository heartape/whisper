package com.heartape.whisper.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 服务器保存avatar和icon时，可以根据id来固定命名，这样在每次上传后都不需要进行更新就可以生效。
 * /avatar/1.jpg
 * /icon/1.jpg
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {

    private String server;

    private Avatar avatar;

    private Icon icon;

    @Getter
    @Setter
    public static class Avatar {

        /**
         * 上传目录（相对路径）
         */
        private String dir;

        /**
         * 访问前缀
         */
        private String prefix;
    }

    @Getter
    @Setter
    public static class Icon {

        /**
         * 上传目录（相对路径）
         */
        private String dir;

        /**
         * 访问前缀
         */
        private String prefix;
    }
}
