/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.notifications;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.clickgui.theme.ThemeManager;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class NotificationRenderer {

    public static final List<Notification> notifications = new ArrayList<>();
    public static final List<Notification> topBarNotifications = new ArrayList<>();
    static final Color topBg = new Color(28, 28, 28, 200);

    public static void render() {
        renderSide();
        renderTop();
    }

    public static void onFastTick() {
        for (Notification notification : new ArrayList<>(notifications)) {
            notification.renderPosX = Transitions.transition(notification.renderPosX, notification.posX, 10);
            notification.renderPosY = Transitions.transition(notification.renderPosY, notification.posY, 10);
            notification.animationProgress = Transitions.transition(notification.animationProgress, notification.animationGoal, 20, 0.0001);
        }
        for (Notification notification : new ArrayList<>(topBarNotifications)) {
            notification.renderPosX = Transitions.transition(notification.renderPosX, notification.posX, 10);
            notification.renderPosY = Transitions.transition(notification.renderPosY, notification.posY, 10);
            if (notification.shouldDoAnimation) {
                notification.animationProgress = Transitions.transition(notification.animationProgress, notification.animationGoal, 10, 0.0001);
            }
        }
    }

    public static void renderTop() {
        MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
        int baseX = CoffeeMain.client.getWindow().getScaledWidth() / 2;
        int height = 16;
        int baseY = -height - 5;
        int currentYOffset = 5;
        float minWidth = 50;
        long c = System.currentTimeMillis();
        ArrayList<Notification> nf = new ArrayList<>(topBarNotifications);
        nf.sort(Comparator.comparingDouble(value -> -FontRenderers.getRenderer().getStringWidth(String.join(" ", value.contents))));
        for (Notification notification : nf) {
            double timeRemaining = Math.abs(c - notification.creationDate - notification.duration) / (double) notification.duration;
            timeRemaining = MathHelper.clamp(timeRemaining, 0, 1);
            boolean notificationExpired = notification.creationDate + notification.duration < c;
            if (notification.duration < 0) {
                timeRemaining = 0;
                notificationExpired = false;
            }
            notification.posX = notification.renderPosX = baseX;
            if (notification.renderPosY == -69 || notification.posY == -69) {
                notification.renderPosY = baseY;
            }
            if (!notificationExpired) {
                notification.posY = currentYOffset;
                if (Math.abs(notification.posY - notification.renderPosY) < 3) {
                    notification.animationGoal = 1;
                }
            } else {
                notification.animationGoal = 0;
                if (notification.animationProgress < 0.01) {
                    notification.posY = baseY - 5;
                    if (notification.renderPosY < baseY + 5) {
                        topBarNotifications.remove(notification);
                    }
                }
            }
            notification.shouldDoAnimation = notification.animationGoal != notification.animationProgress;
            String contents = String.join(" ", notification.contents);
            float width = FontRenderers.getRenderer().getStringWidth(contents) + 5;
            width = width / 2f;
            width = Math.max(minWidth, width);
            ClipStack.globalInstance.addWindow(Renderer.R3D.getEmptyMatrixStack(),
                    new Rectangle(notification.renderPosX - width * notification.animationProgress, notification.renderPosY,
                            notification.renderPosX + width * notification.animationProgress + 1, notification.renderPosY + height + 1));
            //Renderer.R2D.beginScissor(Renderer.R3D.getEmptyMatrixStack(), notification.renderPosX - width * notification.animationProgress, notification.renderPosY, notification.renderPosX + width * notification.animationProgress + 1, notification.renderPosY + height + 1);
            Renderer.R2D.renderQuad(ms, topBg, notification.renderPosX - width, notification.renderPosY, notification.renderPosX + width + 1,
                    notification.renderPosY + height);
            FontRenderers.getRenderer()
                    .drawCenteredString(ms, contents, notification.renderPosX,
                            notification.renderPosY + height / 2f - FontRenderers.getRenderer().getFontHeight() / 2f, 0xFFFFFF);
            double timeRemainingInv = 1 - timeRemaining;
            if (!notification.shouldDoAnimation && notification.animationProgress == 0 && notificationExpired) {
                timeRemainingInv = 1;
            }
            if (notification.duration == -1) {
                double seedR = (System.currentTimeMillis() % 2000) / 2000d;
                double seed = Math.abs((Math.sin(Math.toRadians(seedR * 360)) + 1) / 2);
                Color start = Renderer.Util.lerp(ThemeManager.getMainTheme().getActive(), ThemeManager.getMainTheme().getAccent(), seed);
                Color end = Renderer.Util.lerp(ThemeManager.getMainTheme().getActive(), ThemeManager.getMainTheme().getAccent(), 1 - seed);
                Renderer.R2D.renderQuadGradient(ms, end, start, notification.renderPosX - width, notification.renderPosY + height - 1,
                        notification.renderPosX + width + 1, notification.renderPosY + height, false);
            } else {
                Renderer.R2D.renderQuad(ms, ThemeManager.getMainTheme().getActive(), notification.renderPosX - width, notification.renderPosY + height - 1,
                        notification.renderPosX + width + 1, notification.renderPosY + height);
                Renderer.R2D.renderQuad(ms, ThemeManager.getMainTheme().getAccent(), notification.renderPosX - width, notification.renderPosY + height - 1,
                        notification.renderPosX - width + ((width + 1) * 2 * timeRemainingInv), notification.renderPosY + height);
            }
            ClipStack.globalInstance.popWindow();
            //Renderer.R2D.endScissor();
            currentYOffset += height + 3;
        }
    }

    public static void renderSide() {
        double padding = 10;
        MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
        double yOffset = 0;
        double bottomRightStartX = CoffeeMain.client.getWindow().getScaledWidth() - padding;
        double bottomRightStartY = CoffeeMain.client.getWindow().getScaledHeight() - padding;
        FontAdapter fontRenderer = FontRenderers.getRenderer();
        double texPadding = 4;
        double iconDimensions = 24;
        double minWidth = 100;

        long c = System.currentTimeMillis();
        notifications.removeIf(
                notification -> notification.creationDate + notification.duration < c && Transitions.easeOutExpo(notification.animationProgress) == 0);
        for (Notification notification : new ArrayList<>(notifications)) {
            boolean notificationExpired = notification.creationDate + notification.duration < c;
            notification.animationGoal = notificationExpired ? 0 : 1;
            double contentHeight = 0;
            double contentWidth = 0;
            List<String> content = new ArrayList<>();
            boolean hasTitle = notification.title != null && !notification.title.isEmpty();
            if (hasTitle) {
                contentHeight += fontRenderer.getFontHeight();
                contentWidth = fontRenderer.getStringWidth(notification.title);
                content.add(notification.title);
            }
            String[] nonEmptyContents = Arrays.stream(notification.contents).filter(s -> s != null && !s.isEmpty()).toList().toArray(String[]::new);
            if (nonEmptyContents.length > 0) { // is the array non-null and is any string in there NOT empty?
                contentHeight += nonEmptyContents.length * fontRenderer.getFontHeight();
                for (String contentStr : nonEmptyContents) {
                    contentWidth = Math.max(contentWidth, fontRenderer.getStringWidth(contentStr));
                    content.add(contentStr);
                }
            }


            double notificationHeight = Math.max(iconDimensions, contentHeight) + texPadding * 2d; // always have padding at the outside no matter what
            double notificationWidth = texPadding + iconDimensions + texPadding + Math.max(minWidth,
                    contentWidth) + texPadding; // take padding for the icon into account as well
            double notificationX = notification.posX = bottomRightStartX - notificationWidth;
            double notificationY = bottomRightStartY - notificationHeight - yOffset;
            double interpolatedAnimProgress = Transitions.easeOutExpo(notification.animationProgress);
            Renderer.R2D.renderRoundedQuad(ms, new Color(20, 20, 20, (int) Math.min(255, 255 * interpolatedAnimProgress)), notificationX, notificationY,
                    notificationX + notificationWidth, notificationY + notificationHeight, 5, 20);
            RenderSystem.setShaderTexture(0, notification.type.getI());
            Color notifTheme = notification.type.getC();
            Renderer.setupRender();
            RenderSystem.setShaderColor(notifTheme.getRed() / 255f, notifTheme.getGreen() / 255f, notifTheme.getBlue() / 255f,
                    (float) interpolatedAnimProgress);
            Renderer.R2D.renderTexture(ms, notificationX + texPadding, notificationY + notificationHeight / 2d - iconDimensions / 2d, iconDimensions,
                    iconDimensions, 0, 0, iconDimensions, iconDimensions, iconDimensions, iconDimensions);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            Renderer.endRender();

            double contentHeightAbsolved = 0;
            for (String s : content) {
                fontRenderer.drawString(ms, s, (float) (notificationX + texPadding + iconDimensions + texPadding),
                        (float) (notificationY + notificationHeight / 2d - contentHeight / 2d + contentHeightAbsolved), 1f, 1f, 1f,
                        (float) interpolatedAnimProgress);
                contentHeightAbsolved += fontRenderer.getFontHeight();
            }


            yOffset += (notificationHeight + 5) * interpolatedAnimProgress;
        }
    }
}
