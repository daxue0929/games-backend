package org.daxue.games.manager;

import java.time.Instant;

public class Token {

    private String value;  // Token的实际值
    private Instant expiryTime;  // 过期时间

    // 构造函数
    public Token(String value, Instant expiryTime) {
        this.value = value;
        this.expiryTime = expiryTime;
    }

    // 判断Token是否已过期
    public boolean isExpired() {
        return Instant.now().isAfter(expiryTime);
    }

    // Getters and Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Instant expiryTime) {
        this.expiryTime = expiryTime;
    }
}
