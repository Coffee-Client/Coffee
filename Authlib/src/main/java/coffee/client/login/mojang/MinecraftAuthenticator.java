/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.login.mojang;

import coffee.client.exception.AuthFailureException;
import coffee.client.login.microsoft.MicrosoftAuthenticator;
import coffee.client.login.microsoft.XboxToken;
import coffee.client.login.mojang.profile.MinecraftProfile;
import coffee.client.struct.Authenticator;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MinecraftAuthenticator extends Authenticator<MinecraftToken> {
    protected final MicrosoftAuthenticator microsoftAuthenticator = new MicrosoftAuthenticator();

    public MinecraftToken login(String email, String password) {
        try {
            URL url = new URL("https://authserver.mojang.com/authenticate");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            JsonObject request = new JsonObject();
            JsonObject agent = new JsonObject();
            agent.addProperty("name", "Minecraft");
            agent.addProperty("version", "1");
            request.add("agent", agent);
            request.addProperty("username", email);
            request.addProperty("password", password);
            request.addProperty("requestUser", false);
            String requestBody = request.toString();

            httpURLConnection.setFixedLengthStreamingMode(requestBody.length());
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            // httpURLConnection.setRequestProperty("Host", "authserver.mojang.com");
            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();

            try {
                outputStream.write(requestBody.getBytes(StandardCharsets.US_ASCII));
            } catch (Throwable var13) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                    }
                }

                throw var13;
            }

            outputStream.close();
            JsonObject jsonObject = this.parseResponseData(httpURLConnection);

            return new MinecraftToken(jsonObject.get("accessToken").getAsString(),
                jsonObject.get("selectedProfile").getAsJsonObject().get("name").getAsString(),
                generateUUID(jsonObject.get("selectedProfile").getAsJsonObject().get("id").getAsString()),
                false);
        } catch (IOException var14) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var14.getMessage()));
        }
    }

    public MinecraftToken loginWithMicrosoft(String email, String password) {
        XboxToken xboxToken = this.microsoftAuthenticator.login(email, password);

        try {
            URL url = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            JsonObject request = new JsonObject();
            request.addProperty("identityToken", "XBL3.0 x=" + xboxToken.getUhs() + ";" + xboxToken.getToken());
            String requestBody = request.toString();
            httpURLConnection.setFixedLengthStreamingMode(requestBody.length());
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Host", "api.minecraftservices.com");
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();

            try {
                outputStream.write(requestBody.getBytes(StandardCharsets.US_ASCII));
            } catch (Throwable var13) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                    }
                }

                throw var13;
            }

            outputStream.close();

            JsonObject jsonObject = this.microsoftAuthenticator.parseResponseData(httpURLConnection);
            String accessToken = jsonObject.get("access_token").getAsString();
            MinecraftProfile mp = getGameProfile(accessToken);
            return new MinecraftToken(accessToken, mp.username(), mp.uuid(), true);
        } catch (IOException var14) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var14.getMessage()));
        }
    }

    public MinecraftProfile getGameProfile(String authToken) {
        try {
            URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + authToken);
            httpURLConnection.connect();
            JsonObject jsonObject = this.parseResponseData(httpURLConnection);

            UUID uuid = this.generateUUID(jsonObject.get("id").getAsString());
            String name = jsonObject.get("name").getAsString();


            return new MinecraftProfile(uuid, name);
        } catch (IOException var10) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var10.getMessage()));
        }
    }

    public MinecraftProfile getGameProfile(MinecraftToken minecraftToken) {
        if (isForceMigrated(minecraftToken) && !minecraftToken.isMicrosoft()) {
            // this request is completly useless....
            return new MinecraftProfile(minecraftToken.uuid(), minecraftToken.username());
        }
        return getGameProfile(minecraftToken.accessToken());
    }

    public boolean isForceMigrated(MinecraftToken minecraftToken) {
        try {

            URL url = new URL("https://api.minecraftservices.com/rollout/v1/msamigrationforced");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + minecraftToken.accessToken());
            httpURLConnection.connect();
            JsonObject jsonObject = this.parseResponseData(httpURLConnection);
            return jsonObject.get("rollout").getAsBoolean();
        } catch (IOException var10) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var10.getMessage()));
        }
    }

    public JsonObject parseResponseData(HttpURLConnection httpURLConnection) throws IOException {

        InputStream stream = httpURLConnection.getInputStream();

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return this.gson.fromJson(textBuilder.toString(), JsonObject.class);
    }

    public UUID generateUUID(String trimmedUUID) throws IllegalArgumentException {
        if (trimmedUUID == null) {
            throw new IllegalArgumentException();
        } else {
            StringBuilder builder = new StringBuilder(trimmedUUID.trim());

            try {
                builder.insert(20, "-");
                builder.insert(16, "-");
                builder.insert(12, "-");
                builder.insert(8, "-");
                return UUID.fromString(builder.toString());
            } catch (StringIndexOutOfBoundsException var4) {
                return null;
            }
        }
    }
}
