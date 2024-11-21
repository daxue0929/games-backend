package org.daxue.games.manager;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class TokenCacheManager {

    private final Map<String, Token> caches = new HashMap<>();

    public TokenCacheManager() {
    }

    public Token getCache(String name) {
        return caches.get(name);
    }

    public String setCache(String key, String value, Duration expiryDuration) {
        Token token = new Token(value, Instant.now().plus(expiryDuration));
        caches.put(key, token);
        return key;
    }

    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
}
