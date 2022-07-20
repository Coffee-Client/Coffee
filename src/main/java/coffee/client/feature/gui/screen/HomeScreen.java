/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.ParticleRenderer;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.gui.screen.base.ClientScreen;
import coffee.client.feature.gui.widget.RoundButton;
import coffee.client.helper.CompatHelper;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.GameTexture;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.PlayerHeadResolver;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.Texture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL40C;

import java.awt.Color;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class HomeScreen extends ClientScreen {
    static final double padding = 6;
    static final Texture background = GameTexture.TEXTURE_BACKGROUND.getWhere();
    static boolean isDev = false;
    static String version = "unknown";
    static String changelog = "";
    private static HomeScreen instance;
    final ParticleRenderer prend = new ParticleRenderer(600);
    final FontAdapter propFr = FontRenderers.getCustomSize(22);
    final Texture currentAccountTexture = new Texture("dynamic/tex_currentaccount_home");
    boolean loaded = false;
    long initTime = System.currentTimeMillis();
    boolean fadeOut = false;
    boolean currentAccountTextureLoaded = false;
    UUID previousChecked = null;
    boolean showedCompatWarn = false;

    private HomeScreen() {
        super(MSAAFramebuffer.MAX_SAMPLES);
    }

    public static HomeScreen instance() {
        if (instance == null) {
            instance = new HomeScreen();
        }
        return instance;
    }

    void load() {
        loaded = true;
        try {
            File execF = new File(HomeScreen.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            isDev = execF.isDirectory();
            HomeScreen.version = IOUtils.toString(Objects.requireNonNull(HomeScreen.class.getClassLoader().getResourceAsStream("version.txt")),
                    StandardCharsets.UTF_8
            );
            HomeScreen.changelog = IOUtils.toString(Objects.requireNonNull(HomeScreen.class.getClassLoader().getResourceAsStream("changelogLatest.txt")),
                    StandardCharsets.UTF_8
            );
            updateCurrentAccount(() -> {

            });
            complete();
        } catch (Exception e) {
            e.printStackTrace();
            complete();
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.width = width;
        this.height = height;
        clearChildren();
        initWidgets();
    }

    void initWidgets() {
        List<Map.Entry<String, Runnable>> buttonsMap = new ArrayList<>();
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Singleplayer", () -> CoffeeMain.client.setScreen(new SelectWorldScreen(this))));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Multiplayer", () -> CoffeeMain.client.setScreen(new MultiplayerScreen(this))));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Realms", () -> CoffeeMain.client.setScreen(new RealmsMainScreen(this))));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Alts", () -> {
            //            CoffeeMain.client.setScreen(AltManagerScreen.instance());
            CoffeeMain.client.setScreen(new NotificationScreen(
                    Notification.Type.INFO,
                    "GAMING",
                    "gming moment this is fucking epic abcdajdkhfkjs fsjdkfhsdk fhsdjkf hsjdkfh sdjkfhsjkfh sk",
                    this
            ));
            //CoffeeMain.client.setScreen(new ProxyManagerScreen(this));
        }));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Settings", () -> CoffeeMain.client.setScreen(new OptionsScreen(this, CoffeeMain.client.options))));
        buttonsMap.add(new AbstractMap.SimpleEntry<>("Quit", CoffeeMain.client::scheduleStop));
        double w = 60;
        double rootX = padding * 2 + 20 + padding;
        double rootY = this.height - padding * 2 - 20;

        for (Map.Entry<String, Runnable> stringRunnableEntry : buttonsMap) {
            RoundButton rb = new RoundButton(RoundButton.STANDARD, rootX, rootY, w, 20, stringRunnableEntry.getKey(), stringRunnableEntry.getValue());
            addDrawableChild(rb);
            rootX += w + 5;
        }
    }

    @Override
    protected void init() {
        super.init();
        if (CompatHelper.wereAnyFound() && !showedCompatWarn && client.currentScreen == this) {
            showedCompatWarn = true;
            client.setScreen(new NotificationScreen(
                    Notification.Type.WARNING,
                    "Compatability",
                    "Compatibility issues found, some features might not be available",
                    this
            ));
        }
        initTime = System.currentTimeMillis();
        initWidgets();
        if (loaded) {
            updateCurrentAccount(() -> {
            }); // already loaded this instance, refresh on the fly
        } else {
            load();
        }
    }

    void complete() {
        fadeOut = true;
    }

    void updateCurrentAccount(Runnable callback) {
        UUID uid = CoffeeMain.client.getSession().getProfile().getId();
        if (previousChecked != null && previousChecked.equals(uid)) {
            callback.run();
            return;
        }
        previousChecked = uid;
        PlayerHeadResolver.resolve(uid, this.currentAccountTexture);
        currentAccountTextureLoaded = true;
        callback.run();
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {

        Renderer.R2D.renderQuad(stack, new Color(20, 20, 20), 0, 0, width, height);

        RenderSystem.setShaderTexture(0, background);
        Renderer.R2D.renderTexture(stack, 0, 0, width, height, 0, 0, width, height, width, height);
        RenderSystem.defaultBlendFunc();
        prend.render(stack);

        propFr.drawString(stack, "Changelog", 6, 6, 0xFFFFFF, false);
        double yoff = 6 + propFr.getMarginHeight();
        for (String s : changelog.split("\n")) {
            FontRenderers.getRenderer().drawString(stack, s, 6, (float) yoff, 0xAAAAAA, false);
            yoff += FontRenderers.getRenderer().getMarginHeight();
        }

        double origW = 1024, origH = 1024;
        double newH = 20;
        double per = newH / origH;
        double newW = origW * per;
        Renderer.R2D.renderRoundedQuadWithShadow(stack,
                new Color(0, 0, 10, 200),
                padding,
                height - padding - padding - 20 - padding,
                width - padding,
                height - padding,
                10,
                20
        );
        RenderSystem.setShaderTexture(0, GameTexture.TEXTURE_ICON.getWhere());
        Renderer.R2D.renderTexture(stack, padding * 2, height - padding * 2 - newH, newW, newH, 0, 0, newW, newH, newW, newH);
        super.renderInternal(stack, mouseX, mouseY, delta); // render bottom row widgets

        double texDim = 20;
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Renderer.R2D.renderRoundedQuadInternal(stack.peek().getPositionMatrix(),
                0,
                0,
                0,
                1,
                width - padding - texDim,
                padding,
                width - padding,
                padding + texDim,
                3,
                10
        );

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderTexture(0, currentAccountTextureLoaded ? currentAccountTexture : DefaultSkinHelper.getTexture());
        if (currentAccountTextureLoaded) {
            Renderer.R2D.renderTexture(stack, width - padding - texDim, padding, texDim, texDim, 0, 0, 64, 64, 64, 64);
        } else {
            Renderer.R2D.renderTexture(stack, width - padding - texDim, padding, texDim, texDim, 8, 8, 8, 8, 64, 64);
        }
        FontAdapter fa = FontRenderers.getRenderer();
        RenderSystem.defaultBlendFunc();
        String uname = CoffeeMain.client.getSession().getUsername();
        double unameWidth = fa.getStringWidth(uname);
        fa.drawString(stack, uname, width - padding - texDim - padding - unameWidth, padding + texDim / 2d - fa.getFontHeight() / 2d, 0xFFFFFF);
    }

}
