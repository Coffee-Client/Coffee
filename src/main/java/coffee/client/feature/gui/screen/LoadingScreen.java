/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.textures.Texture;
import coffee.client.helper.util.Utils;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Data;
import lombok.SneakyThrows;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoadingScreen extends AAScreen implements FastTickable {
    static final int atOnce = 1;
    static LoadingScreen INSTANCE = null;
    final AtomicBoolean loaded = new AtomicBoolean(false);
    final AtomicBoolean loadInProg = new AtomicBoolean(false);
    double opacity = 1;
    String warningIfPresent = "";

    private LoadingScreen() {
        super(MSAAFramebuffer.MAX_SAMPLES);
    }

    public static LoadingScreen instance() {
        if (INSTANCE == null) {
            INSTANCE = new LoadingScreen();
        }
        return INSTANCE;
    }

    @Override
    protected void initInternal() {
        HomeScreen.instance().init(client, width, height);
        if (loaded.get() && opacity == 0.001) {
            client.setScreen(HomeScreen.instance());
        }
        super.initInternal();
    }

    @Override
    public void onFastTick() {
        if (CoffeeMain.client.getOverlay() == null) {
            if (!loadInProg.get()) {
                load();
            }
        }
        if (loaded.get()) {
            opacity -= 0.01;
            opacity = MathHelper.clamp(opacity, 0.001, 1);
        }
    }

    @SneakyThrows
    void load() {
        loadInProg.set(true);

        ExecutorService es = Executors.newFixedThreadPool(atOnce);

        for (Field declaredField : Texture.class.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers()) && Texture.class.isAssignableFrom(declaredField.getType())) {
                Object o = declaredField.get(null);
                Texture tex = (Texture) o;
                es.execute(() -> {
                    CoffeeMain.log(Level.INFO, "Loading " + tex);
                    try {
                        tex.load();
                        CoffeeMain.log(Level.INFO, "Loading " + tex);
                    } catch (Throwable t) {
                        CoffeeMain.log(Level.ERROR, "Failed to load " + tex);
                        t.printStackTrace();
                        warningIfPresent = "Some textures failed to download. They won't show up in game.";
                    }
                });
            }
        }

        new Thread(() -> {
            es.shutdown();
            try {
                //noinspection ResultOfMethodCallIgnored
                es.awaitTermination(99999, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (!warningIfPresent.isEmpty()) {
                    Utils.sleep(2000);
                }
                loaded.set(true);
            }

        }, "Loader").start();
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {

        if (loaded.get()) {
            HomeScreen.instance().renderInternal(stack, mouseX, mouseY, delta);
            if (opacity == 0.001) {
                assert this.client != null;
                this.client.setScreen(HomeScreen.instance());
                return;
            }
        }
        Renderer.R2D.renderQuad(stack, new Color(0f, 0f, 0f, (float) opacity), 0, 0, width, height);

        double anim = (System.currentTimeMillis() % 1000) / 1000d;
        String dots = ".".repeat((int) Math.max(Math.ceil(anim * 3), 1));
        FontRenderers.getRenderer().drawString(stack, "Loading textures" + dots, 3, height - FontRenderers.getRenderer().getFontHeight() - 3, 0.7f, 0.7f, 0.7f, (float) opacity);
        stack.push();
        double texDim = 32;
        stack.translate(width / 2d, height / 2d, 0);
        double rot = (System.currentTimeMillis() % 2000) / 2000d * 180;
        float sin = (float) Math.sin(Math.toRadians(rot));
        stack.scale(1f + MathHelper.lerp(sin, 0f, 0.2f), 1f + MathHelper.lerp(sin, 0f, 0.2f), 1);
        Texture.ICON.bind();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, (float) opacity);
        Renderer.R2D.renderTexture(stack, -texDim / 2, -texDim / 2, texDim, texDim, 0, 0, texDim, texDim, texDim, texDim);
        stack.pop();
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    @Data
    static class ProgressData {
        AtomicDouble progress = new AtomicDouble(0);
        AtomicDouble progressSmooth = new AtomicDouble(0);
        AtomicBoolean workingOnIt = new AtomicBoolean(false);
    }
}
