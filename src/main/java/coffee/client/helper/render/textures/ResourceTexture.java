/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render.textures;

import coffee.client.helper.render.Rectangle;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceTexture implements Texture {
    String path;
    Rectangle cachedBounds = new Rectangle(0, 0, 0, 0);
    coffee.client.helper.render.Texture spriteId;
    AtomicBoolean initialized = new AtomicBoolean(false);

    public ResourceTexture(String path) {
        this.path = path;
        this.spriteId = new coffee.client.helper.render.Texture("texture/direct/" + RandomStringUtils.randomAlphanumeric(16));
    }

    @SneakyThrows
    void ensureInitialized() {
        if (!initialized.get()) {
            load();
        }
    }

    @Override
    public void load() throws Throwable {
        if (initialized.get()) {
            return;
        }
        initialized.set(true);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource " + path + " not found");
            }
            BufferedImage bi = ImageIO.read(is);
            cachedBounds.setX(0);
            cachedBounds.setY(0);
            cachedBounds.setX1(bi.getWidth());
            cachedBounds.setY1(bi.getHeight());
            Utils.registerBufferedImageTexture(spriteId, bi);
        }
    }

    @Override
    public void bind() {
        ensureInitialized();
        RenderSystem.setShaderTexture(0, spriteId);
    }

    @Override
    public Rectangle getBounds() {
        return cachedBounds;
    }

    @Override
    public boolean alreadyInitialized() {
        return true;
    }

    @Override
    public String toString() {
        return "ResourceTexture{" + "path='" + path + '\'' + '}';
    }
}
