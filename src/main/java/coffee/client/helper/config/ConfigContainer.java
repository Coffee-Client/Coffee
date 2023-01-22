/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.config;

import coffee.client.helper.gson.GsonSupplier;
import coffee.client.helper.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("unused")
public class ConfigContainer {
    static final Gson gson = GsonSupplier.getGson();
    private static final int[] EXPECTED_SIGNATURE = new int[] { 0xC0, 0xFF, 0xEE, 0x00 };
    final File path;
    @Getter
    JsonElement value;
    boolean loaded = false;

    public ConfigContainer(File f) {
        this(f, new JsonObject());
    }

    public ConfigContainer(File f, JsonElement defaultValue) {
        this.path = f;
        this.value = defaultValue;
        reload();
    }

    private static String decompress(byte[] data) {
        return Utils.throwSilently(() -> {
            if (data.length < 4) {
                return new String(data);
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(data);

            int[] firstFourBits = new int[4];
            byte[] sig = new byte[4];
            bais.read(sig, 0, 4);
            for (int i = 0; i < firstFourBits.length; i++) {
                firstFourBits[i] = Byte.toUnsignedInt(sig[i]);
            }
            if (!Arrays.equals(firstFourBits, EXPECTED_SIGNATURE)) {
                return new String(data); // probably legacy format
            }
            GZIPInputStream gi = new GZIPInputStream(bais);
            byte[] bytes = gi.readAllBytes();
            return new String(bytes);
        }, Throwable::printStackTrace);
    }

    public <T> T get(Class<T> type) {
        if (!loaded) {
            return null;
        }
        return gson.fromJson(getValue(), type);
    }

    public void set(Object data) {
        set(gson.toJsonTree(data));
    }

    public void set(JsonElement obj) {
        value = obj;
    }

    void write(String data) {
        byte[] b = data.getBytes(StandardCharsets.UTF_8);
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream go = new GZIPOutputStream(baos);

            // signature
            for (int i : EXPECTED_SIGNATURE) {
                fos.write(i);
            }

            // compress data
            go.write(b);
            go.flush();
            go.close();

            // write compressed data
            fos.write(baos.toByteArray());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        if (!path.exists()) {
            return;
        }
        try {
            byte[] t = FileUtils.readFileToByteArray(path);
            String p = decompress(t);
            if (p == null) {
                throw new IllegalStateException("Invalid data format");
            }
            set(JsonParser.parseString(p));
            loaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        write(value.toString());
    }
}
