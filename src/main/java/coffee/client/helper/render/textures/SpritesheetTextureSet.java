/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render.textures;

import coffee.client.helper.gson.GsonSupplier;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Cleanup;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpritesheetTextureSet implements Texture {
    static HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    String spritesheet;
    Map<String, TextureEntry> textures = new HashMap<>();
    coffee.client.helper.render.Texture spriteId;
    Rectangle cachedBounds = new Rectangle(0, 0, 0, 0);

    public SpritesheetTextureSet(String spritesheetUrl, TextureEntry... entries) {
        this.spritesheet = spritesheetUrl;
        for (TextureEntry entry : entries) {
            this.textures.put(entry.name, entry);
        }
        this.spriteId = new coffee.client.helper.render.Texture("sprite/" + RandomStringUtils.randomAlphanumeric(16));
    }

    public static SpritesheetTextureSet fromJson(String url, String json) {
        SpritesheetJsonRepr spritesheetJsonRepr = GsonSupplier.getGson().fromJson(json, SpritesheetJsonRepr.class);
        List<TextureEntry> entries = new ArrayList<>();
        for (SpritesheetJsonRepr.SpritesheetJsonEntry frame : spritesheetJsonRepr.frames) {
            TextureEntry e = new TextureEntry(frame.name, frame.frame.x, frame.frame.y, frame.frame.w, frame.frame.h);
            entries.add(e);
        }
        return new SpritesheetTextureSet(url, entries.toArray(TextureEntry[]::new));
    }

    @Override
    public void load() throws Throwable {
        URI uri = URI.create(spritesheet);
        HttpRequest get = HttpRequest.newBuilder().uri(uri).header("User-Agent", "coffee/1.0").build();
        HttpResponse<InputStream> send = client.send(get, HttpResponse.BodyHandlers.ofInputStream());
        System.out.println(send.toString());
        @Cleanup InputStream body = send.body();
        BufferedImage read = ImageIO.read(body);
        Utils.registerBufferedImageTexture(spriteId, read);
        this.cachedBounds.setX1(read.getWidth());
        this.cachedBounds.setY1(read.getHeight());
    }

    public TextureEntry bindAndGetBounds(String name) {
        TextureEntry textureEntry = this.textures.get(name);
        if (textureEntry == null) {
            throw new IllegalArgumentException(String.format("Texture \"%s\" does not exist", name));
        }
        bind();
        return textureEntry;
    }

    public void bindAndDraw(MatrixStack stack, double x, double y, double width, double height, String name) {
        TextureEntry textureEntry = bindAndGetBounds(name);
        Matrix4f matrix = stack.peek().getPositionMatrix();
        float z = 0;
        float x0 = (float) x;
        float x1 = (float) (x + width);
        float y0 = (float) y;
        float y1 = (float) (y + height);
        float u0 = (float) (textureEntry.x / getBounds().getWidth());
        float v0 = (float) (textureEntry.y / getBounds().getHeight());
        float u1 = (float) ((textureEntry.x + textureEntry.w) / getBounds().getWidth());
        float v1 = (float) ((textureEntry.y + textureEntry.h) / getBounds().getHeight());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x0, y1, z).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x1, y1, z).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x1, y0, z).texture(u1, v0).next();
        bufferBuilder.vertex(matrix, x0, y0, z).texture(u0, v0).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
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
        return "SpritesheetTextureSet{" + "spritesheet='" + spritesheet + '\'' + ", textures=" + textures + '}';
    }

    public record TextureEntry(String name, int x, int y, int w, int h) {

    }
}
