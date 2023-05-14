/*
 * Copyright (c) 2023 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.login;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.ToString;
import net.minecraft.util.Util;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class MicrosoftLogin {
    private static final String CLIENT_ID = "4673b348-3efa-4f6a-bbb6-34e141cdc638";
    private static final int PORT = 9675;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static HttpServer server;
    private static Consumer<String> callback;

    public static void getRefreshToken(Consumer<String> callback) {
        MicrosoftLogin.callback = callback;

        startServer();
        Util.getOperatingSystem()
            .open("https://login.live.com/oauth20_authorize.srf?client_id=" + CLIENT_ID + "&response_type=code&redirect_uri=http://127.0.0.1:" + PORT +
                "&scope=XboxLive.signin%20offline_access&prompt=select_account");
    }

    @SneakyThrows
    private static <T> T sendHttp(HttpRequest req, Class<T> responseType) {
        HttpResponse<String> send = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(send.body(), responseType);
    }

    @SneakyThrows
    public static LoginData login(String refreshToken) {
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://login.live.com/oauth20_token.srf"))
            .POST(HttpRequest.BodyPublishers.ofString(
                "client_id=" + CLIENT_ID + "&refresh_token=" + refreshToken + "&grant_type=refresh_token&redirect_uri=http://127.0.0.1:" + PORT))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();
        // Refresh access token
        AuthTokenResponse res = sendHttp(req, AuthTokenResponse.class);

        if (res == null) {
            return new LoginData();
        }

        String accessToken = res.access_token;
        refreshToken = res.refresh_token;

        // XBL
        XblXstsResponse xblRes = sendHttp(HttpRequest.newBuilder(URI.create("https://user.auth.xboxlive.com/user/authenticate"))
            .POST(HttpRequest.BodyPublishers.ofString(
                "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + accessToken +
                    "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}"))
            .header("Content-Type", "application/json")
            .build(), XblXstsResponse.class);

        if (xblRes == null) {
            return new LoginData();
        }

        // XSTS
        XblXstsResponse xstsRes = sendHttp(HttpRequest.newBuilder(URI.create("https://xsts.auth.xboxlive.com/xsts/authorize"))
            .POST(HttpRequest.BodyPublishers.ofString("{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblRes.Token +
                "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}"))
            .header("Content-Type", "application/json")
            .build(), XblXstsResponse.class);

        if (xstsRes == null) {
            return new LoginData();
        }

        // Minecraft
        McResponse mcRes = sendHttp(HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/authentication/login_with_xbox"))
            .POST(HttpRequest.BodyPublishers.ofString("{\"identityToken\":\"XBL3.0 x=" + xblRes.DisplayClaims.xui[0].uhs + ";" + xstsRes.Token + "\"}"))
            .header("Content-Type", "application/json")
            .build(), McResponse.class);

        if (mcRes == null) {
            return new LoginData();
        }

        // Check game ownership
        GameOwnershipResponse gameOwnershipRes = sendHttp(HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/entitlements/mcstore"))
            .header("Authorization", "Bearer " + mcRes.access_token)
            .build(), GameOwnershipResponse.class);

        if (gameOwnershipRes == null || !gameOwnershipRes.hasGameOwnership()) {
            return new LoginData();
        }

        // Profile
        ProfileResponse profileRes = sendHttp(HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/minecraft/profile"))
            .header("Authorization", "Bearer " + mcRes.access_token)
            .build(), ProfileResponse.class);

        if (profileRes == null) {
            return new LoginData();
        }

        return new LoginData(mcRes.access_token, refreshToken, profileRes.id, profileRes.name);
    }

    private static void startServer() {
        if (server != null) {
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);

            server.createContext("/", new Handler());
            //            server.setExecutor(MeteorExecutor.executor);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void stopServer() {
        if (server == null) {
            return;
        }

        server.stop(0);
        server = null;

        callback = null;
    }

    @ToString
    public static class LoginData {
        public String mcToken;
        public String newRefreshToken;
        public String uuid, username;

        public LoginData() {
        }

        public LoginData(String mcToken, String newRefreshToken, String uuid, String username) {
            this.mcToken = mcToken;
            this.newRefreshToken = newRefreshToken;
            this.uuid = uuid;
            this.username = username;
        }

        public boolean isGood() {
            return mcToken != null;
        }
    }

    private static class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange req) {
            try {
                if (req.getRequestMethod().equals("GET")) {
                    // Login
                    List<NameValuePair> query = URLEncodedUtils.parse(req.getRequestURI(), StandardCharsets.UTF_8);

                    boolean ok = false;

                    for (NameValuePair pair : query) {
                        if (pair.getName().equals("code")) {
                            handleCode(pair.getValue());

                            ok = true;
                            break;
                        }
                    }

                    if (!ok) {
                        writeText(req, "Cannot authenticate.");
                    } else {
                        writeText(req, "You may now close this page.");
                    }
                }

                stopServer();
            } catch (Throwable t) {
                t.printStackTrace();

            }
        }

        private void handleCode(String code) {
            AuthTokenResponse res = sendHttp(HttpRequest.newBuilder(URI.create("https://login.live.com/oauth20_token.srf"))
                .POST(HttpRequest.BodyPublishers.ofString(
                    "client_id=" + CLIENT_ID + "&code=" + code + "&grant_type=authorization_code&redirect_uri=http://127.0.0.1:" + PORT))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build(), AuthTokenResponse.class);

            if (res == null) {
                callback.accept(null);
            } else {
                callback.accept(res.refresh_token);
            }
        }

        private void writeText(HttpExchange req, String text) throws IOException {
            OutputStream out = req.getResponseBody();

            req.sendResponseHeaders(200, text.length());

            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
    }

    private static class AuthTokenResponse {
        public String access_token;
        public String refresh_token;
    }

    private static class XblXstsResponse {
        public String Token;
        public DisplayClaims DisplayClaims;

        private record DisplayClaims(XblXstsResponse.DisplayClaims.Claim[] xui) {

            private record Claim(String uhs) {
            }
        }
    }

    private static class McResponse {
        public String access_token;
    }

    private record GameOwnershipResponse(MicrosoftLogin.GameOwnershipResponse.Item[] items) {

        private boolean hasGameOwnership() {
            boolean hasProduct = false;
            boolean hasGame = false;

            for (Item item : items) {
                if (item.name.equals("product_minecraft")) {
                    hasProduct = true;
                } else if (item.name.equals("game_minecraft")) {
                    hasGame = true;
                }
            }

            return hasProduct && hasGame;
        }

        private record Item(String name) {
        }
    }

    private static class ProfileResponse {
        public String id;
        public String name;
    }
}
