package me.x150.analytics;

import coffee.client.feature.gui.screen.HomeScreen;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

/*
 * Helper class for analytics
 *
 * === DISCLAIMER ===
 * This does not send sensitive information, and only tracks when the app is launched.
 * It **does** send the **HASHED** mac address of the launched user to track reoccurig launches vs new launches. The mac is hashed with SHA256
 * You can disable analytics entirely by launching the game with the "x150.disableAnalytics" property. Add this to your start parameters: "-Dx150.disableAnalytics=true"
 * Running the mod in dev environments (such as in the IDE) will also not send analytics.
 * I am being completely transparent with this info, all the code is documented so you can see exactly what it does.
 * **Do not complain that this exists, you can disable it at any time. This is just here for me to see if this mod is actually used**
 */
public class Analytics {
    /**
     * The flag for disabling analytics. Set with {@code "-Dx150.disableAnalytics=true"} in the java parameters.
     */
    static final String DISABLE_ANALYTICS_PROPERTY = "x150.disableAnalytics";
    /**
     * The console logger for information printed to console
     */
    static final Logger logger = LogManager.getLogger("analytics");

    /**
     * Is this being ran in a dev environment?
     */
    static final boolean isInDevEnv = Util.make(() -> {
        try {
            // Get jarfile being executed
            File runLocation = new File(Analytics.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // is the "jarfile" being executed a folder?
            return runLocation.isDirectory();
//            return false;
        } catch (Exception e) {
            // can't check, assume false
            return false;
        }
    });

    static final String version = Util.make(() -> {
        try {
            return IOUtils.toString(Objects.requireNonNull(HomeScreen.class.getClassLoader().getResourceAsStream("version.txt")), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "unknown";
        }
    });

    /**
     * Http client with timeout of 5 seconds
     */
    private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    /**
     * Telemetry uri to send analytics to
     */
    private static final URI telemetryURI = URI.create("https://track.client.coffee/analytics");
    String modName;

    public Analytics(String name) {
        this.modName = name;
    }

    /**
     * @return Whether this user is in a dev env or not
     * @see #isInDevEnv
     */
    public static boolean isInDevEnvironment() {
        return isInDevEnv;
    }

    /**
     * Should we disable analytics?
     *
     * @return Whether or not to disable analytics
     */
    public static boolean shouldDisableAnalytics() {
        // either we're in a dev environment or the system property is set to true
        return isInDevEnvironment() || System.getProperty(DISABLE_ANALYTICS_PROPERTY, "false").equalsIgnoreCase("true");
    }

    private static String getMac() {
        String unk = "UNKNOWN";
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.fatal("Failed to get MAC:");
            e.printStackTrace();
            return unk;
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            byte[] hardwareAddress;
            try {
                hardwareAddress = ni.getHardwareAddress();
            } catch (SocketException e) {
                logger.fatal("Failed to get MAC:");
                e.printStackTrace();
                return unk;
            }
            if (hardwareAddress != null) {
                String[] hexadecimalFormat = new String[hardwareAddress.length];
                for (int i = 0; i < hardwareAddress.length; i++) {
                    hexadecimalFormat[i] = String.format("%02X", hardwareAddress[i]);
                }
                return String.join(":", hexadecimalFormat).toLowerCase();
            }
        }
        return unk;
    }

    private static String hashSha256(String in) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(in.getBytes(StandardCharsets.UTF_8));
            byte[] d = digest.digest();
            return String.format("%064x", new BigInteger(1, d));
        } catch (Exception e) {
            logger.fatal("Failed to hash input string \"" + in + "\":");
            e.printStackTrace();
            // default to unhashed if hashing fails
            return in;
        }
    }

    /**
     * Sends a telemetry event
     *
     * @param event The event to send
     */
    private void send(TelemetryEvent event) {
        // make a new http request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(telemetryURI)
                .header("User-Agent", "x150_analytics/1.0")
                .header("X-Session", hashSha256(getMac()))
                .header("X-Source", modName)
                .header("content-type", "application/json")
                // post data to post
                .POST(HttpRequest.BodyPublishers.ofString(event.toJObject().toString()))
                .build();
        // send the data async so we don't block the main thread
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse -> {
            // debug
            logger.debug("Server responded: " + stringHttpResponse.toString());
            logger.debug(stringHttpResponse.body());
        }).exceptionally(throwable -> {
            // debug
            logger.info("Failed to send analytics:");
            throwable.printStackTrace();
            return null;
        });
    }

    /**
     * The game has been launched
     */
    public void gameLaunched() {
        // should we disable analytics?
        if (shouldDisableAnalytics()) {
            // if yes, don't send anything
            logger.info("Not sending analytics due to " + DISABLE_ANALYTICS_PROPERTY + " being set or being launched in dev environment");
        } else {
            // if not, send the gameLaunched event
            TelemetryEvent launchedEvent = new TelemetryEvent("gameLaunched", Map.of("clientVersion", version));
            send(launchedEvent);
        }
    }

    /**
     * A telemetry event
     *
     * @param id     The telemetry event id
     * @param params Additional info
     */
    record TelemetryEvent(String id, Map<String, Object> params) {
        public JsonObject toJObject() {
            Gson gson = new Gson();
            JsonObject jo = new JsonObject();
            jo.addProperty("id", id);
            JsonElement je = gson.toJsonTree(params);
            jo.add("data", je);
            return jo;
        }
    }
}
