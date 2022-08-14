/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.gson;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class GsonSupplier {
    private static final Gson gsonInstance = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        registerAdapters(builder);
        builder.setPrettyPrinting();
        return builder.create();
    });

    public static Gson getGson() {
        return gsonInstance;
    }

    private static void registerAdapters(GsonBuilder builder) {
        JsonDeserializer<Color> colorDeser = (jsonElement, type, jsonDeserializationContext) -> {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            int r = getAsIntOrDefault(asJsonObject.get("r"), 255);
            int g = getAsIntOrDefault(asJsonObject.get("g"), 255);
            int b = getAsIntOrDefault(asJsonObject.get("b"), 255);
            int a = getAsIntOrDefault(asJsonObject.get("a"), 255);
            Preconditions.checkArgument(r >= 0 && r <= 255);
            Preconditions.checkArgument(g >= 0 && g <= 255);
            Preconditions.checkArgument(b >= 0 && b <= 255);
            Preconditions.checkArgument(a >= 0 && a <= 255);
            return new Color(r, g, b, a);
        };
        JsonSerializer<Color> colorSer = (color, type, jsonSerializationContext) -> {
            JsonObject jval = new JsonObject();
            jval.addProperty("r", color.getRed());
            jval.addProperty("g", color.getGreen());
            jval.addProperty("b", color.getBlue());
            jval.addProperty("a", color.getAlpha());
            return jval;
        };
        builder.registerTypeAdapter(Color.class, colorDeser);
        builder.registerTypeAdapter(Color.class, colorSer);

        JsonDeserializer<Vec3d> vec3dJsonDeserializer = (jsonElement, type, jsonDeserializationContext) -> {
            JsonObject jobj = jsonElement.getAsJsonObject();
            double x = getAsDoubleOrDefault(jobj.get("x"), 0d);
            double y = getAsDoubleOrDefault(jobj.get("y"), 0d);
            double z = getAsDoubleOrDefault(jobj.get("z"), 0d);
            return new Vec3d(x, y, z);
        };

        JsonSerializer<Vec3d> vec3dJsonSerializer = (vec3d, type, jsonSerializationContext) -> {
            JsonObject jobj = new JsonObject();
            jobj.addProperty("x", vec3d.x);
            jobj.addProperty("y", vec3d.y);
            jobj.addProperty("z", vec3d.z);
            return jobj;
        };

        builder.registerTypeAdapter(Vec3d.class, vec3dJsonDeserializer);
        builder.registerTypeAdapter(Vec3d.class, vec3dJsonSerializer);
    }

    private static int getAsIntOrDefault(JsonElement el, int defaultVal) {
        return el == null ? defaultVal : el.getAsInt();
    }

    private static double getAsDoubleOrDefault(JsonElement el, double defaultVal) {
        return el == null ? defaultVal : el.getAsDouble();
    }
}
