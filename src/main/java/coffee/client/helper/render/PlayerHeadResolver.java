/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.render;

import coffee.client.CoffeeMain;
import coffee.client.helper.Texture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.apache.logging.log4j.Level;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHeadResolver {
    static final NativeImageBackedTexture EMPTY = new NativeImageBackedTexture(1, 1, false);
    static final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    static final Map<UUID, NativeImageBackedTexture> imageCache = new HashMap<>();

    public static void resolve(UUID uuid, Texture texture) {
        if (imageCache.containsKey(uuid)) {
            CoffeeMain.client.execute(() -> CoffeeMain.client.getTextureManager()
                    .registerTexture(texture, imageCache.get(uuid)));
            return;
        }
        imageCache.put(uuid, EMPTY);
        CoffeeMain.client.execute(() -> CoffeeMain.client.getTextureManager().registerTexture(texture, EMPTY));
        URI u = URI.create("https://mc-heads.net/avatar/" + uuid);
        HttpRequest hr = HttpRequest.newBuilder().uri(u).header("user-agent", "coffee/1.0").build();
        CoffeeMain.log(Level.DEBUG, "Getting player head for", uuid);
        client.sendAsync(hr, HttpResponse.BodyHandlers.ofByteArray()).thenAccept(httpResponse -> {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ImageIO.write(ImageIO.read(new ByteArrayInputStream(httpResponse.body())), "png", stream);
                byte[] bytes = stream.toByteArray();

                ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
                data.flip();
                NativeImage img = NativeImage.read(data);
                NativeImageBackedTexture nib = new NativeImageBackedTexture(img);

                CoffeeMain.client.execute(() -> CoffeeMain.client.getTextureManager().registerTexture(texture, nib));
                imageCache.put(uuid, nib);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

    }

    public static Texture resolve(UUID uuid) {
        Texture tex = new Texture(String.format("skin_preview-%s", uuid.toString()));
        resolve(uuid, tex);
        return tex;
    }
}
