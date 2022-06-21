/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.helper.GameTexture;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Data;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoadingScreen extends AAScreen implements FastTickable {
    static final int atOnce = 2;
    static LoadingScreen INSTANCE = null;
    final AtomicBoolean loaded = new AtomicBoolean(false);
    final AtomicBoolean loadInProg = new AtomicBoolean(false);
    final Map<GameTexture, ProgressData> progressMap = new ConcurrentHashMap<>();
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

    GameTexture[] getTexturesForDownload() {
        return Arrays.stream(GameTexture.values()).filter(gameTexture -> !gameTexture.isAlreadyInitialized()).toList().toArray(GameTexture[]::new);
    }

    @Override
    public void onFastTick() {
        for (ProgressData value : progressMap.values()) {
            value.getProgressSmooth().set(Transitions.transition(value.getProgressSmooth().get(), value.getProgress().get(), 7, 0));
        }
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

    void load() {
        loadInProg.set(true);

        ExecutorService es = Executors.newFixedThreadPool(atOnce);


        for (GameTexture resource : getTexturesForDownload()) {
            progressMap.put(resource, new ProgressData());
            es.execute(() -> {
                CoffeeMain.log(Level.INFO, "Downloading " + resource.getDownloadUrl());
                progressMap.get(resource).getWorkingOnIt().set(true);
                try {

                    URL url = new URL(resource.getDownloadUrl());
                    HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                    long completeFileSize = httpConnection.getContentLength();

                    BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] data = new byte[8];
                    long downloadedFileSize = 0;
                    int x;
                    progressMap.get(resource).getProgress().set(0.1);
                    while ((x = in.read(data, 0, data.length)) >= 0) {
                        downloadedFileSize += x;

                        double currentProgress = ((double) downloadedFileSize) / ((double) completeFileSize);
                        progressMap.get(resource).getProgress().set(currentProgress * 0.8 + 0.1);

                        bout.write(data, 0, x);
                    }
                    bout.close();
                    in.close();
                    byte[] imageBuffer = bout.toByteArray();
                    BufferedImage bi = ImageIO.read(new ByteArrayInputStream(imageBuffer));
                    resource.getDimensions().setX(0);
                    resource.getDimensions().setY(0);
                    resource.getDimensions().setX1(bi.getWidth());
                    resource.getDimensions().setY1(bi.getHeight());
                    Utils.registerBufferedImageTexture(resource.getWhere(), bi);
                    CoffeeMain.log(Level.INFO, "Downloaded " + resource.getDownloadUrl());
                } catch (Exception e) {
                    CoffeeMain.log(Level.ERROR, "Failed to download " + resource.getDownloadUrl() + ": " + e.getMessage());
                    BufferedImage empty = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                    empty.setRGB(0, 0, 0xFF000000);
                    Utils.registerBufferedImageTexture(resource.getWhere(), empty);
                    warningIfPresent = "Some textures failed to download. They won't show up in game.";
                } finally {
                    progressMap.get(resource).getProgress().set(1);
                    progressMap.get(resource).getWorkingOnIt().set(false);
                }
            });
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
        int o = (int) (opacity * 255);
        Renderer.R2D.renderQuad(stack, new Color(0, 0, 0, o), 0, 0, width, height);

        List<ProgressData> progM = new ArrayList<>(progressMap.values());
        double xOffset = 0;
        double oneWidth = (double) width / progM.size();
        for (int i = 0; i < progM.size(); i++) {
            double index = (double) i / progM.size();
            ProgressData progressData = progM.get(i);
            double progress = progressData.getProgressSmooth().get();
            double wid = progress * oneWidth;
            Color c = Color.getHSBColor((float) index, 0.6f, 1f);
            Renderer.R2D.renderQuad(stack, Renderer.Util.modify(c, -1, -1, -1, o), xOffset, 0, xOffset + wid, 2);
            xOffset += wid;
        }
        stack.push();
        double texDim = 32;
        stack.translate(width / 2d, height / 2d, 0);
        double rot = (System.currentTimeMillis() % 2000) / 2000d * 180;
        float sin = (float) Math.sin(Math.toRadians(rot));
        stack.scale(1f + MathHelper.lerp(sin, 0f, 0.2f), 1f + MathHelper.lerp(sin, 0f, 0.2f), 1);
        RenderSystem.setShaderTexture(0, GameTexture.TEXTURE_ICON.getWhere());
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
