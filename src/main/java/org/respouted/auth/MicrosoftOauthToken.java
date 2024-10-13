package org.respouted.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class MicrosoftOauthToken {
    // token type is always bearer
    public final String accessToken;
    public final Date expiry;
    public final String scope;
    public final String refreshToken;
    public final String clientId;
    public final String redirectUri;

    /**
     * @param accessToken The main access token
     * @param expiresInSeconds How many seconds it'll expire in
     * @param scope The scope used in the first oauth request
     * @param refreshToken The refresh token used to create a new access token once this one expires
     * @param clientId The client id used to make the first oauth request
     * @param redirectUri The redirect uri used to make the first oauth request
     */
    public MicrosoftOauthToken(String accessToken, int expiresInSeconds, String scope, String refreshToken, String clientId, String redirectUri) {
        this(accessToken, Date.from(Instant.now().plus(expiresInSeconds, ChronoUnit.SECONDS)), scope, refreshToken, clientId, redirectUri);
    }

    /**
     * @param accessToken The main access token
     * @param expiry When the token expires
     * @param scope The scope used in the first oauth request
     * @param refreshToken The refresh token used to create a new access token once this one expires
     * @param clientId The client id used to make the first oauth request
     * @param redirectUri The redirect uri used to make the first oauth request
     */
    public MicrosoftOauthToken(String accessToken, Date expiry, String scope, String refreshToken, String clientId, String redirectUri) {
        this.accessToken = accessToken;
        this.expiry = expiry;
        this.scope = scope;
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public boolean isExpired() {
        return expiry.before(new Date());
    }

    @Override
    public String toString() {
        return "Access token: " + accessToken
                + "\nExpiry date: " + expiry
                + "\nScope: " + scope
                + "\nRefresh token: " + refreshToken
                + "\nClient ID: " + clientId
                + "\nRedirect URI: " + redirectUri;
    }
}
