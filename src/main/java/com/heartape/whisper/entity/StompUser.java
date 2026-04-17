package com.heartape.whisper.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@Getter
@AllArgsConstructor
public class StompUser implements Principal {
    private final String userId;
    private final String name;

    @Override
    public String getName() {
        return userId;
    }

}