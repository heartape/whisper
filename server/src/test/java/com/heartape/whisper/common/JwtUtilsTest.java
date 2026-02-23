package com.heartape.whisper.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilsTest {

    @BeforeEach
    void init() {
        JwtUtils.init(
                "3qX8LZ2dX5b4M0N7qF6pKQw0X6JH5bKcZlRZpYJ0zH8=",
                604800000,
                "server",
                "Android"
        );
    }

    @Test
    void generate() {
        System.out.println("user1: " + JwtUtils.generate("1"));
        System.out.println("user2: " + JwtUtils.generate("2"));
    }

    @Test
    void testGenerate() {
    }

    @Test
    void check() {
    }

    @Test
    void parse() {
    }
}