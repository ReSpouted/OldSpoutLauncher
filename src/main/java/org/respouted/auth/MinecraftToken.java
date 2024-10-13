package org.respouted.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class MinecraftToken {
    public final String accessToken;
    public final Date expiry;

    public MinecraftToken(String accessToken, int expiresInSeconds) {
        this(accessToken, Date.from(Instant.now().plus(expiresInSeconds, ChronoUnit.SECONDS)));
    }

    public MinecraftToken(String accessToken, Date expiry) {
        this.accessToken = accessToken;
        this.expiry = expiry;
    }
}
