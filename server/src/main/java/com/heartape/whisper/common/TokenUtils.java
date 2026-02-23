package com.heartape.whisper.common;

import jakarta.annotation.Nonnull;

public class TokenUtils {

    private final static String BEAR = "Bearer ";

    private final static String TOKEN_STORE_PREFIX = "TOKEN:";

    public static boolean isBearerToken(String bearerToken){
        return bearerToken != null && bearerToken.startsWith(BEAR);
    }

    public static String createBearerToken(String token){
        return BEAR + token;
    }

    public static String getTokenFromBearerToken(String bearerToken){
        return bearerToken.substring(BEAR.length());
    }

    public static String createTokenStoreKey(@Nonnull String key){
        return TOKEN_STORE_PREFIX + key;
    }
}
