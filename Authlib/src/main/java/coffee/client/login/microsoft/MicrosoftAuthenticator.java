

package coffee.client.login.microsoft;

import coffee.client.exception.AuthFailureException;
import coffee.client.struct.Authenticator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MicrosoftAuthenticator extends Authenticator<XboxToken> {
    protected String loginUrl;
    protected String loginCookie;
    protected String loginPPFT;

    public MicrosoftAuthenticator() {
    }

    public XboxToken login(String email, String password) {
        MicrosoftToken microsoftToken = this.generateTokenPair(this.generateLoginCode(email, password));
        XboxLiveToken xboxLiveToken = this.generateXboxTokenPair(microsoftToken);
        return this.generateXboxTokenPair(xboxLiveToken);
    }

    private String generateLoginCode(String email, String password) {
        try {
            URL url = new URL("https://login.live.com/oauth20_authorize.srf?redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=code&locale=en&client_id=00000000402b5328");
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            InputStream inputStream = httpURLConnection.getResponseCode() == 200 ? httpURLConnection.getInputStream() : httpURLConnection.getErrorStream();
            this.loginCookie = httpURLConnection.getHeaderField("set-cookie");
            String responseData = (String)(new BufferedReader(new InputStreamReader(inputStream))).lines().collect(Collectors.joining());
            Matcher bodyMatcher = Pattern.compile("sFTTag: ?'.*value=\"(.*)\"/>'").matcher(responseData);
            if (bodyMatcher.find()) {
                this.loginPPFT = bodyMatcher.group(1);
                bodyMatcher = Pattern.compile("urlPost: ?'(.+?(?='))").matcher(responseData);
                if (!bodyMatcher.find()) {
                    throw new AuthFailureException("Authentication error. Could not find 'LOGIN-URL' tag from response!");
                } else {
                    this.loginUrl = bodyMatcher.group(1);
                    if (this.loginCookie != null && this.loginPPFT != null && this.loginUrl != null) {
                        return this.sendCodeData(email, password);
                    } else {
                        throw new AuthFailureException("Authentication error. Error in authentication process!");
                    }
                }
            } else {
                throw new AuthFailureException("Authentication error. Could not find 'LOGIN-PFTT' tag from response!");
            }
        } catch (IOException var8) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var8.getMessage()));
        }
    }

    private String sendCodeData(String email, String password) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("login", email);
        requestData.put("loginfmt", email);
        requestData.put("passwd", password);
        requestData.put("PPFT", this.loginPPFT);
        String postData = this.encodeURL(requestData);

        String authToken;
        try {
            byte[] data = postData.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection connection = (HttpURLConnection)(new URL(this.loginUrl)).openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            connection.setRequestProperty("Cookie", this.loginCookie);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();

            try {
                outputStream.write(data);
            } catch (Throwable var12) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                    }
                }

                throw var12;
            }

            outputStream.close();

            if (connection.getResponseCode() != 200 || connection.getURL().toString().equals(this.loginUrl)) {
                throw new AuthFailureException("Authentication error. Username or password is not valid.");
            }

            Pattern pattern = Pattern.compile("[?|&]code=([\\w.-]+)");
            Matcher tokenMatcher = pattern.matcher(URLDecoder.decode(connection.getURL().toString(), StandardCharsets.UTF_8));
            if (!tokenMatcher.find()) {
                throw new AuthFailureException("Authentication error. Could not handle data from response.");
            }

            authToken = tokenMatcher.group(1);
        } catch (IOException var13) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var13.getMessage()));
        }

        this.loginUrl = null;
        this.loginCookie = null;
        this.loginPPFT = null;
        return authToken;
    }

    private void sendXboxRequest(HttpURLConnection httpURLConnection, JsonObject request, JsonObject properties) throws IOException {
        request.add("Properties", properties);
        String requestBody = request.toString();
        httpURLConnection.setFixedLengthStreamingMode(requestBody.length());
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.connect();
        OutputStream outputStream = httpURLConnection.getOutputStream();

        try {
            outputStream.write(requestBody.getBytes(StandardCharsets.US_ASCII));
        } catch (Throwable var9) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable var8) {
                    var9.addSuppressed(var8);
                }
            }

            throw var9;
        }

        outputStream.close();

    }

    private MicrosoftToken generateTokenPair(String authToken) {
        try {
            Map<String, String> arguments = new HashMap<>();
            arguments.put("client_id", "00000000402b5328");
            arguments.put("code", authToken);
            arguments.put("grant_type", "authorization_code");
            arguments.put("redirect_uri", "https://login.live.com/oauth20_desktop.srf");
            arguments.put("scope", "service::user.auth.xboxlive.com::MBI_SSL");
            StringJoiner argumentBuilder = new StringJoiner("&");

            for (Map.Entry<String, String> entry : arguments.entrySet()) {
                argumentBuilder.add(this.encodeURL(entry.getKey()) + "=" + this.encodeURL(entry.getValue()));
            }

            byte[] data = argumentBuilder.toString().getBytes(StandardCharsets.UTF_8);
            URL url = new URL("https://login.live.com/oauth20_token.srf");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setFixedLengthStreamingMode(data.length);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();

            try {
                outputStream.write(data);
            } catch (Throwable var12) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                    }
                }

                throw var12;
            }

            outputStream.close();

            JsonObject jsonObject = this.parseResponseData(httpURLConnection);
            return new MicrosoftToken(jsonObject.get("access_token").getAsString(), jsonObject.get("refresh_token").getAsString());
        } catch (IOException var13) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var13.getMessage()));
        }
    }

    public XboxLiveToken generateXboxTokenPair(MicrosoftToken microsoftToken) {
        try {
            URL url = new URL("https://user.auth.xboxlive.com/user/authenticate");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
            httpURLConnection.setDoOutput(true);
            JsonObject request = new JsonObject();
            request.addProperty("RelyingParty", "http://auth.xboxlive.com");
            request.addProperty("TokenType", "JWT");
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", microsoftToken.getToken());
            this.sendXboxRequest(httpURLConnection, request, properties);
            JsonObject jsonObject = this.parseResponseData(httpURLConnection);
            String uhs = ((JsonObject)jsonObject.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0)).get("uhs").getAsString();
            return new XboxLiveToken(jsonObject.get("Token").getAsString(), uhs);
        } catch (IOException var9) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var9.getMessage()));
        }
    }

    public XboxToken generateXboxTokenPair(XboxLiveToken xboxLiveToken) {
        try {
            URL url = new URL("https://xsts.auth.xboxlive.com/xsts/authorize");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            JsonObject request = new JsonObject();
            request.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            request.addProperty("TokenType", "JWT");
            JsonObject properties = new JsonObject();
            properties.addProperty("SandboxId", "RETAIL");
            JsonArray userTokens = new JsonArray();
            userTokens.add(xboxLiveToken.getToken());
            properties.add("UserTokens", userTokens);
            this.sendXboxRequest(httpURLConnection, request, properties);
            if (httpURLConnection.getResponseCode() == 401) {
                throw new AuthFailureException("No xbox account was found!");
            } else {
                JsonObject jsonObject = this.parseResponseData(httpURLConnection);
                String uhs = ((JsonObject)jsonObject.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0)).get("uhs").getAsString();
                return new XboxToken(jsonObject.get("Token").getAsString(), uhs);
            }
        } catch (IOException var10) {
            throw new AuthFailureException(String.format("Authentication error. Request could not be made! Cause: '%s'", var10.getMessage()));
        }
    }

    public JsonObject parseResponseData(HttpURLConnection httpURLConnection) throws IOException {
        BufferedReader bufferedReader;
        if (httpURLConnection.getResponseCode() != 200) {
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
        } else {
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        }

        String lines = bufferedReader.lines().collect(Collectors.joining());
        JsonObject jsonObject = JsonParser.parseString(lines).getAsJsonObject();
        if (jsonObject.has("error")) {
            throw new AuthFailureException(jsonObject.get("error") + ": " + jsonObject.get("error_description"));
        } else {
            return jsonObject;
        }
    }

    private String encodeURL(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    private String encodeURL(Map<String, String> map) {
        return map.keySet().stream().map(s -> String.format("%s=%s", encodeURL(s), encodeURL(map.get(s)))).collect(Collectors.joining("&"));
    }
}
