/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render.textures;

import coffee.client.helper.render.Rectangle;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Cleanup;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DirectTexture implements Texture {
    static HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    String url;
    Rectangle cachedBounds = new Rectangle(0, 0, 0, 0);
    coffee.client.helper.render.Texture spriteId;

    public DirectTexture(String url) {
        this.url = url;
        this.spriteId = new coffee.client.helper.render.Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));
    }

    @Override
    public void load() throws Throwable {
        URI uri = URI.create(url);
        HttpRequest get = HttpRequest.newBuilder().uri(uri).header("User-Agent", "coffee/1.0").build();
        HttpResponse<InputStream> send = client.send(get, HttpResponse.BodyHandlers.ofInputStream());
        @Cleanup InputStream body = send.body();
        BufferedImage read = ImageIO.read(body);
        Utils.registerBufferedImageTexture(spriteId, read);
        this.cachedBounds.setX1(read.getWidth());
        this.cachedBounds.setY1(read.getHeight());
    }

    @Override
    public void bind() {
        RenderSystem.setShaderTexture(0, spriteId);
    }

    @Override
    public Rectangle getBounds() {
        return cachedBounds;
    }

    @Override
    public String toString() {
        return "DirectTexture{" + "url='" + url + '\'' + '}';
    }
}
