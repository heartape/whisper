package com.heartape.whisper.exception;

public class SystemException extends RuntimeException {

    private final int code;

    public SystemException(String msg) {
        super(msg);
        this.code = 400;
    }

    public SystemException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
