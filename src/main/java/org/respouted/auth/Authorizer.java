package org.respouted.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.respouted.Main;
import org.respouted.Storage;
import org.respouted.Util;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.stream.Collectors;

// https://wiki.vg/Microsoft_Authentication_Scheme
// https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-auth-code-flow
public class Authorizer {
    public static boolean initialized = false;

    public static void initialize() {
        initialized = true;
        MicrosoftOauthToken oauthToken = Storage.INSTANCE.getMicrosoftOauthToken();
        if(oauthToken != null) {
            if(oauthToken.isExpired()) {
                oauthToken = getMicrosoftOauthToken(new OauthResponse(oauthToken.refreshToken, oauthToken.scope, oauthToken.redirectUri, oauthToken.clientId));
                Storage.INSTANCE.setMicrosoftOauthToken(oauthToken);
            }
            MinecraftToken minecraftToken = getMinecraftToken(getXboxXstsToken(getXboxXblToken(oauthToken)));
            Storage.INSTANCE.setMinecraftToken(minecraftToken);
            Profile profile = getProfile(minecraftToken);
            Storage.INSTANCE.setProfile(profile);
        }
    }

    public static boolean ownsMinecraft() {
        if(!initialized) {
            throw new IllegalStateException("The authorizer hasn't been initialized!");
        }

        return Storage.INSTANCE.getMicrosoftOauthToken() != null && Storage.INSTANCE.getProfile() != null && Storage.INSTANCE.getProfile().exists;
    }

    public static Profile getProfile(MinecraftToken minecraftToken) {
        // we can check for ownership while getting the profile, so the extra request to entitlements/mcstore is
        // unnecessary

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URI("https://api.minecraftservices.com/minecraft/profile").toURL().openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + minecraftToken.accessToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            JSONObject response = new JSONObject(Util.getResponseBody(responseCode, connection));
            if(response.has("id")) {
                return new Profile(response.getString("id"), response.getString("name"));
            }

            // does not own the game
            return new Profile();
        } catch(IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public static MinecraftToken getMinecraftToken(XboxXstsToken xboxXstsToken) {
        JSONObject request = new JSONObject()
                .put("identityToken", "XBL3.0 x=" + xboxXstsToken.userHash + ";" + xboxXstsToken.token);

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URI("https://api.minecraftservices.com/authentication/login_with_xbox").toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(request.toString().getBytes(StandardCharsets.UTF_8));

            int responseCode = connection.getResponseCode();
            JSONObject response = new JSONObject(Util.getResponseBody(responseCode, connection));

            return new MinecraftToken(response.getString("access_token"), response.getInt("expires_in"));
        } catch(URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static XboxXstsToken getXboxXstsToken(XboxXblToken xboxXblToken) {
        JSONObject request = new JSONObject()
                .put("Properties", new JSONObject()
                        .put("SandboxId", "RETAIL")
                        .put("UserTokens", new JSONArray().put(xboxXblToken.token)))
                .put("RelyingParty", "rp://api.minecraftservices.com/")
                .put("TokenType", "JWT");

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URI("https://xsts.auth.xboxlive.com/xsts/authorize").toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(request.toString().getBytes(StandardCharsets.UTF_8));

            int responseCode = connection.getResponseCode();
            JSONObject response = new JSONObject(Util.getResponseBody(responseCode, connection));

            //TODO handle non-200

            return new XboxXstsToken(response.getString("IssueInstant"),
                    response.getString("NotAfter"),
                    response.getString("Token"),
                    response.getJSONObject("DisplayClaims").getJSONArray("xui").getJSONObject(0).getString("uhs"));
        } catch(URISyntaxException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static XboxXblToken getXboxXblToken(MicrosoftOauthToken microsoftOauthToken) {
        JSONObject request = new JSONObject()
                .put("Properties", new JSONObject()
                        .put("AuthMethod", "RPS")
                        .put("SiteName", "user.auth.xboxlive.com")
                        .put("RpsTicket", "d=" + microsoftOauthToken.accessToken))
                .put("RelyingParty", "http://auth.xboxlive.com")
                .put("TokenType", "JWT");

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URI("https://user.auth.xboxlive.com/user/authenticate").toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(request.toString().getBytes(StandardCharsets.UTF_8));

            //TODO handle non-200

            int responseCode = connection.getResponseCode();
            JSONObject response = new JSONObject(Util.getResponseBody(responseCode, connection));
            return new XboxXblToken(response.getString("IssueInstant"),
                    response.getString("NotAfter"),
                    response.getString("Token"),
                    response.getJSONObject("DisplayClaims").getJSONArray("xui").getJSONObject(0).getString("uhs"));
        } catch(URISyntaxException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static MicrosoftOauthToken getMicrosoftOauthToken(OauthResponse oauthResponse) {
        try {
            URI uri = new URI("https://login.microsoftonline.com/consumers/oauth2/v2.0/token");

            String body = "client_id=" + oauthResponse.clientId + "&"
                    + "scope=" + oauthResponse.scope + "&"
                    + "code=" + oauthResponse.code + "&"
                    + "redirect_uri=" + oauthResponse.redirectURI + "&"
                    + "grant_type=authorization_code";
            HttpsURLConnection connection =  (HttpsURLConnection) uri.toURL().openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().close();

            int responseCode = connection.getResponseCode();
            String responseMessage = Util.getResponseBody(responseCode, connection);
            JSONObject response = new JSONObject(responseMessage);
            return new MicrosoftOauthToken(response.getString("access_token"),
                    response.getInt("expires_in"),
                    response.getString("scope"),
                    response.getString("refresh_token"),
                    oauthResponse.clientId,
                    oauthResponse.redirectURI);

        } catch(URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static OauthResponse doMicrosoftInteractiveAuthorization(String clientId) {
        try {
            final String[] code = {null};
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            httpServer.createContext("/", exchange -> {
                for(String[] query : Arrays.stream(exchange.getRequestURI().getQuery().split("&")).map(q -> q.split("=")).collect(Collectors.toList())) {
                    if(query.length != 2) continue;
                    if(!"code".equals(query[0])) continue;
                    code[0] = query[1];
                    break;
                }
                synchronized(code) {
                    code.notifyAll();
                }

                byte[] response = "Logged in successfully. You may now close this window.".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.getResponseBody().close();

            });
            httpServer.start();

            String redirectURI = "http%3A%2F%2Flocalhost%3A" + httpServer.getAddress().getPort();
            String scope = "XboxLive.signin%20XboxLive.offline_access";

            URI uri = new URI("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?"
                     + "client_id=" + clientId + "&"
                     + "scope=" + scope + "&"
                     + "response_type=code&"
                     + "redirect_uri=" + redirectURI + "&"
                     + "prompt=select_account");

            Main.window.loggingInDialog.updateLink(uri.toString());
            try {
                // I don't know what caused this https://github.com/ATLauncher/ATLauncher/issues/713, but I'd rather
                // play it safe and make sure it's possible to log in even if that happens. This is also why the
                // url is given to the user to copy-paste as a fallback.
                Desktop.getDesktop().browse(uri);
            } catch(IOException | UnsupportedOperationException ignored) {
            }

            synchronized(code) {
                try {
                    code.wait();
                } catch(InterruptedException e) {
                    httpServer.stop(0);
                    throw new RuntimeException(e);
                }
            }
            httpServer.stop(1);

            return new OauthResponse(code[0], scope, redirectURI, clientId);
        } catch(IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }



    public static class OauthResponse {
        public final String code;
        public final String scope;
        public final String redirectURI;
        public final String clientId;

        public OauthResponse(String code, String scope, String redirectURI, String clientId) {
            this.code = code;
            this.scope = scope;
            this.redirectURI = redirectURI;
            this.clientId = clientId;
        }

    }
}
