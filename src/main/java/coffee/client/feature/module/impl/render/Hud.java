/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.gui.hud.HudRenderer;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.gui.notifications.NotificationRenderer;
import coffee.client.feature.gui.theme.ThemeManager;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.AccurateFrameRateCounter;
import coffee.client.helper.util.Timer;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import coffee.client.mixin.render.IInGameHudMixin;
import coffee.client.mixin.screen.IDebugHudMixin;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Hud extends Module {
    public static double currentTps = 0;
    public final BooleanSetting speed = this.config.create(new BooleanSetting.Builder(true).name("Speed").description("Show your current velocity").get());
    final DateFormat minSec = new SimpleDateFormat("mm:ss");
    final BooleanSetting fps = this.config.create(new BooleanSetting.Builder(true).name("FPS").description("Whether to show FPS").get());
    final BooleanSetting tps = this.config.create(new BooleanSetting.Builder(true).name("TPS").description("Whether to show TPS").get());
    final BooleanSetting coords = this.config.create(new BooleanSetting.Builder(true).name("Coordinates").description("Whether to show current coordinates").get());
    final BooleanSetting ping = this.config.create(new BooleanSetting.Builder(true).name("Ping").description("Whether to show current ping").get());
    final BooleanSetting modules = this.config.create(new BooleanSetting.Builder(true).name("Array list").description("Whether to show currently enabled modules").get());
    final Timer tpsUpdateTimer = new Timer();
    final List<Double> last5SecondTpsAverage = new ArrayList<>();
    final Map<Module, ModuleEntry> entryList = new ConcurrentHashMap<>();
    long lastTimePacketReceived;
    double rNoConnectionPosY = -10d;
    Notification serverNotResponding = null;


    public Hud() {
        super("Hud", "Shows information about the player on screen", ModuleType.RENDER);
        lastTimePacketReceived = System.currentTimeMillis();

        Events.registerEventHandler(EventType.PACKET_RECEIVE, event1 -> {
            PacketEvent event = (PacketEvent) event1;
            if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
                lastTimePacketReceived = System.currentTimeMillis();
            }
        }, 0);
    }

    double calcTps(double n) {
        return (20.0 / Math.max((n - 1000.0) / (500.0), 1.0));
    }

    @Override
    public void tick() {
        long averageTime = 5000;
        long waitTime = 100;
        long maxLength = averageTime / waitTime;
        if (tpsUpdateTimer.hasExpired(waitTime)) {
            tpsUpdateTimer.reset();
            double newTps = calcTps(System.currentTimeMillis() - lastTimePacketReceived);
            last5SecondTpsAverage.add(newTps);
            while (last5SecondTpsAverage.size() > maxLength) {
                last5SecondTpsAverage.remove(0);
            }
            currentTps = Utils.Math.roundToDecimal(last5SecondTpsAverage.stream().reduce(Double::sum).orElse(0d) / last5SecondTpsAverage.size(), 2);

        }
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {
        if (CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        if (CoffeeMain.client.player == null) {
            return;
        }
        makeSureIsInitialized();
        MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
        double heightOffsetLeft = 0, heightOffsetRight = 0;
        if (CoffeeMain.client.options.debugEnabled) {
            double heightAccordingToMc = 9;
            List<String> lt = ((IDebugHudMixin) ((IInGameHudMixin) CoffeeMain.client.inGameHud).getDebugHud()).callGetLeftText();
            List<String> rt = ((IDebugHudMixin) ((IInGameHudMixin) CoffeeMain.client.inGameHud).getDebugHud()).callGetRightText();
            heightOffsetLeft = 2 + heightAccordingToMc * (lt.size() + 3);
            heightOffsetRight = 2 + heightAccordingToMc * rt.size() + 5;
        }
        if (!shouldNoConnectionDropDown()) {
            if (serverNotResponding != null) {
                serverNotResponding.duration = 0;
            }
        } else {
            if (serverNotResponding == null) {
                serverNotResponding = Notification.create(-1,
                    "",
                    true,
                    Notification.Type.INFO,
                    "Server not responding! " + minSec.format(System.currentTimeMillis() - lastTimePacketReceived));
            }
            serverNotResponding.contents = new String[] { "Server not responding! " + minSec.format(System.currentTimeMillis() - lastTimePacketReceived) };
        }
        if (!NotificationRenderer.topBarNotifications.contains(serverNotResponding)) {
            serverNotResponding = null;
        }

        if (modules.getValue()) {
            ms.push();
            ms.translate(0, heightOffsetRight, 0);
            drawModuleList(ms);
            ms.pop();
        }

        ms.push();
        ms.translate(0, heightOffsetLeft, 0);
        drawTopLeft(ms);
        ms.pop();

        HudRenderer.getInstance().render();


    }

    public void drawTopLeft(MatrixStack ms) {
        ms.translate(5, 5, 0);
        List<String> values = new ArrayList<>();
        if (this.fps.getValue()) {
            values.add(AccurateFrameRateCounter.globalInstance.getFps() + " fps");
        }

        if (this.tps.getValue()) {
            String tStr = currentTps + "";
            String[] dotS = tStr.split("\\.");
            String tpsString = dotS[0];
            if (!dotS[1].equalsIgnoreCase("0")) {
                tpsString += "." + dotS[1];
            }
            values.add(tpsString + " tps");
        }
        if (this.ping.getValue()) {
            PlayerListEntry ple = Objects.requireNonNull(CoffeeMain.client.getNetworkHandler())
                .getPlayerListEntry(Objects.requireNonNull(CoffeeMain.client.player).getUuid());
            values.add((ple == null || ple.getLatency() == 0 ? "?" : ple.getLatency() + "") + " ms");
        }
        if (this.coords.getValue()) {
            BlockPos bp = Objects.requireNonNull(CoffeeMain.client.player).getBlockPos();
            values.add(bp.getX() + " " + bp.getY() + " " + bp.getZ());
        }
        double pad = 2;

        double originalIconWidth = 1024;
        double originalIconHeight = 1024;

        double newWidth = 16;
        double delta = newWidth / originalIconWidth;
        double newHeight = originalIconHeight * delta;

        String desc = String.join(" | ", values);

        double width = pad + newWidth + 5 + FontRenderers.getRenderer().getStringWidth(desc) + pad;
        double height = pad * 2 + Math.max(newHeight, FontRenderers.getRenderer().getFontHeight());
        Renderer.R2D.renderRoundedQuadWithShadow(ms, ThemeManager.getMainTheme().getConfig(), 0, 0, width, height, 5, 20);
        coffee.client.helper.render.textures.Texture.ICON.bind();
        Renderer.R2D.renderTexture(ms, pad, height / 2d - newHeight / 2d, newWidth, newHeight, 0, 0, newWidth, newHeight, newWidth, newHeight);
        FontRenderers.getRenderer().drawString(ms, desc, pad + newWidth + 5, height / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d, 0xFFFFFF);
    }

    void drawModuleList(MatrixStack ms) {
        double width = CoffeeMain.client.getWindow().getScaledWidth();
        double y = 0;
        for (Map.Entry<Module, ModuleEntry> moduleEntry : this.entryList.entrySet()
            .stream()
            .sorted(Comparator.comparingDouble(value -> -value.getValue().getRenderWidth()))
            .toList()) {
            double prog = moduleEntry.getValue().getAnimProg() * 2;
            if (prog == 0) {
                continue;
            }
            double expandProg = MathHelper.clamp(prog, 0, 1); // 0-1 as 0-1 from 0-2
            double slideProg = MathHelper.clamp(prog - 1, 0, 1); // 1-2 as 0-1 from 0-2
            double hei = (FontRenderers.getRenderer().getMarginHeight() + 2);
            double wid = moduleEntry.getValue().getRenderWidth() + 2;
            Renderer.R2D.renderQuad(ms, ThemeManager.getMainTheme().getActive(), width - (wid + 1), y, width, y + hei * expandProg);
            ms.push();
            ms.translate((1 - slideProg) * wid, 0, 0);
            Renderer.R2D.renderQuad(ms, ThemeManager.getMainTheme().getModule(), width - wid, y, width, y + hei * expandProg);
            double nameW = FontRenderers.getRenderer().getStringWidth(moduleEntry.getKey().getName());
            FontRenderers.getRenderer().drawString(ms, moduleEntry.getKey().getName(), width - wid + 1, y + 1, 0xFFFFFF);
            if (moduleEntry.getKey().getContext() != null && !moduleEntry.getKey().getContext().isEmpty()) {
                FontRenderers.getRenderer().drawString(ms, " " + moduleEntry.getKey().getContext(), width - wid + 1 + nameW, y + 1, 0xAAAAAA);
            }
            ms.pop();
            y += hei * expandProg;
        }

    }

    void makeSureIsInitialized() {
        for (Module module : ModuleRegistry.getModules()) {
            if (!entryList.containsKey(module)) {
                ModuleEntry me = new ModuleEntry();
                me.module = module;
                entryList.put(module, me);
            }
        }
        for (Map.Entry<Module, ModuleEntry> moduleModuleEntryEntry : entryList.entrySet()) {
            if (!ModuleRegistry.getModules().contains(moduleModuleEntryEntry.getKey()) && moduleModuleEntryEntry.getValue().animationProgress == 0) {
                entryList.remove(moduleModuleEntryEntry.getKey());
            }
        }
    }

    @Override
    public void onFastTick() {
        rNoConnectionPosY = Transitions.transition(rNoConnectionPosY, shouldNoConnectionDropDown() ? 10 : -10, 10);
        HudRenderer.getInstance().fastTick();
        for (ModuleEntry moduleEntry : entryList.values()) {
            moduleEntry.animate();
        }
    }

    boolean shouldNoConnectionDropDown() {
        return System.currentTimeMillis() - lastTimePacketReceived > 2000;
    }

    static class ModuleEntry {
        Module module;
        double animationProgress = 0;
        double renderWidth = getWidth();

        void animate() {
            double a = 0.02;
            if (module == null || !module.isEnabled()) {
                a *= -1;
            }
            animationProgress += a;
            animationProgress = MathHelper.clamp(animationProgress, 0, 1);
            renderWidth = Transitions.transition(renderWidth, getWidth(), 7, 0);
        }

        double getAnimProg() {
            return easeInOutCirc(animationProgress);
        }

        String getDrawString() {
            if (module == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(module.getName());
            if (module.getContext() != null && !module.getContext().isEmpty()) {
                sb.append(" ").append(module.getContext());
            }
            return sb.toString();
        }

        double getWidth() {
            return FontRenderers.getRenderer().getStringWidth(getDrawString());
        }

        double getRenderWidth() {
            return renderWidth;
        }

        double easeInOutCirc(double x) {
            return x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2;

        }
    }

}
