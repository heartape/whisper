package com.heartape.whisper.util;

public class FileUtils {

    private final static String SERVER = "https://gravatar.com/avatar/%s?s=200&d=monsterid";

    public static String avatar(String id) {
        return SERVER.formatted(id);
    }

    public static String icon(String id) {
        return SERVER.formatted(id);
    }
}
