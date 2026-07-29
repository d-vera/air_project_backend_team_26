package com.airproject.airproject.security;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final Set<String> blacklistedJtis = ConcurrentHashMap.newKeySet();

    public void blacklist(String jti) {
        if (jti != null) {
            blacklistedJtis.add(jti);
        }
    }

    public boolean isBlacklisted(String jti) {
        return jti != null && blacklistedJtis.contains(jti);
    }
}
