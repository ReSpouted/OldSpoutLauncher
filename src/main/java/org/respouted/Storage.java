package org.respouted;

import org.json.JSONObject;
import org.respouted.auth.MicrosoftOauthToken;
import org.respouted.auth.MinecraftToken;
import org.respouted.auth.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class Storage {
    public static Storage INSTANCE = new Storage();

    private MicrosoftOauthToken microsoftOauthToken = null;
    private MinecraftToken minecraftToken = null;
    private Profile profile = null;
    private static final String MICROSOFT_OAUTH_TOKEN_FILENAME = "oauth_token.json";
    private static final String PROFILE_FILENAME = "profile.json";

    /**
     * Gets the token object, either stored in a variable from an earlier time or from a file called
     * <code>{@value #MICROSOFT_OAUTH_TOKEN_FILENAME}</code> in the working directory.
     * @return The token object, or null if there is no file representing that object or if the contents of that file
     *         are invalid.
     */
    public MicrosoftOauthToken getMicrosoftOauthToken() {
        if(microsoftOauthToken != null) return microsoftOauthToken;

        try (InputStream inputStream = Files.newInputStream(Paths.get(MICROSOFT_OAUTH_TOKEN_FILENAME))) {
            JSONObject object = new JSONObject(Util.readEntireStream(inputStream));
            microsoftOauthToken = new MicrosoftOauthToken(object.getString("accessToken"),
                    new Date(object.getLong("expiry")),
                    object.getString("scope"),
                    object.getString("refreshToken"),
                    object.getString("clientId"),
                    object.getString("redirectUri"));
            return microsoftOauthToken;
        } catch(IOException e) {
            return null;
        }
    }

    // maybe this should throw IOException? not sure... I doubt it could happen accidentally
    /**
     * Stores the token object on a variable to quickly retrieve with {@link #getMicrosoftOauthToken()} and on a file called
     * <code>{@value #MICROSOFT_OAUTH_TOKEN_FILENAME}</code> in the working directory.
     * @param microsoftOauthToken The token object to be set, or null if it needs to be removed.
     */
    public void setMicrosoftOauthToken(MicrosoftOauthToken microsoftOauthToken) {
        this.microsoftOauthToken = microsoftOauthToken;

        if(microsoftOauthToken != null) {
            JSONObject object = new JSONObject()
                    .put("accessToken", microsoftOauthToken.accessToken)
                    .put("expiry", microsoftOauthToken.expiry.getTime())
                    .put("scope", microsoftOauthToken.scope)
                    .put("refreshToken", microsoftOauthToken.refreshToken)
                    .put("clientId", microsoftOauthToken.clientId)
                    .put("redirectUri", microsoftOauthToken.redirectUri);

            try(OutputStream outputStream = Files.newOutputStream(Paths.get(MICROSOFT_OAUTH_TOKEN_FILENAME))) {
                outputStream.write(object.toString().getBytes(StandardCharsets.UTF_8));
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Files.delete(Paths.get(MICROSOFT_OAUTH_TOKEN_FILENAME));
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public MinecraftToken getMinecraftToken() {
        return minecraftToken;
    }

    public void setMinecraftToken(MinecraftToken minecraftToken) {
        this.minecraftToken = minecraftToken;
    }

    public Profile getProfile() {
        if(profile != null) return profile;

        try (InputStream inputStream = Files.newInputStream(Paths.get(PROFILE_FILENAME))) {
            JSONObject object = new JSONObject(Util.readEntireStream(inputStream));
            return new Profile(object.getString("uuid"), object.getString("username"));
        } catch(IOException e) {
            return null;
        }
    }

    public void setProfile(Profile profile) {
        this.profile = profile;

        if(profile != null) {
            try (OutputStream outputStream = Files.newOutputStream(Paths.get(PROFILE_FILENAME))) {
                JSONObject object = new JSONObject()
                        .put("uuid", profile.uuid)
                        .put("username", profile.username);
                outputStream.write(object.toString().getBytes(StandardCharsets.UTF_8));
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Files.delete(Paths.get(PROFILE_FILENAME));
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
