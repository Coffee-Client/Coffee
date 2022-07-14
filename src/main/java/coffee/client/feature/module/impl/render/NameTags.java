/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.awt.Color;
import java.util.Comparator;

public class NameTags extends Module {
    public NameTags() {
        super("NameTags", "Shows information about players above them", ModuleType.RENDER);
    }


    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    public void render(MatrixStack stack, AbstractClientPlayerEntity entity, Text text) {
        String t = text.getString();

        Vec3d headPos = Utils.getInterpolatedEntityPosition(entity).add(0, entity.getHeight() + 0.3, 0);
        Vec3d a = Renderer.R2D.getScreenSpaceCoordinate(headPos, stack);
        if (Renderer.R2D.isOnScreen(a)) {
            Utils.TickManager.runOnNextRender(() -> drawInternal(a, t, entity));
        }
    }

    void drawInternal(Vec3d screenPos, String text, AbstractClientPlayerEntity entity) {
        FontAdapter nameDrawer = FontRenderers.getRenderer();
        FontAdapter infoDrawer = FontRenderers.getCustomSize(12);
        double healthHeight = 2;
        double labelHeight = 2 + nameDrawer.getFontHeight() + infoDrawer.getFontHeight() + 2 + healthHeight + 2;
        int ping = -1;
        GameMode gamemode = null;
        PlayerListEntry ple = client.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        if (ple != null) {
            gamemode = ple.getGameMode();
            ping = ple.getLatency();
        }
        String pingStr = (ping == 0 ? "?" : ping) + " ms";
        String gmString = "§cBot";
        if (gamemode != null) {
            switch (gamemode) {
                case ADVENTURE -> gmString = "Adventure";
                case CREATIVE -> gmString = "Creative";
                case SURVIVAL -> gmString = "Survival";
                case SPECTATOR -> gmString = "Spectator";
            }
        }
        MatrixStack stack1 = Renderer.R3D.getEmptyMatrixStack();
        Vec3d actual = new Vec3d(screenPos.x, screenPos.y - labelHeight, screenPos.z);
        float width = nameDrawer.getStringWidth(text) + 4;
        width = Math.max(width, 60);

        Renderer.R2D.renderRoundedQuad(stack1, new Color(0, 0, 5, 100), actual.x - width / 2d, actual.y, actual.x + width / 2d, actual.y + labelHeight, 3, 20);
        nameDrawer.drawString(stack1, text, actual.x + width / 2d - nameDrawer.getStringWidth(text) - 2, actual.y + 2, 0xFFFFFF);

        infoDrawer.drawString(stack1,
                gmString,
                actual.x + width / 2d - infoDrawer.getStringWidth(gmString) - 2,
                actual.y + 2 + nameDrawer.getFontHeight(),
                0xAAAAAA
        );
        if (ping != -1) {
            infoDrawer.drawString(stack1, pingStr, actual.x - width / 2d + 2, actual.y + 2 + nameDrawer.getFontHeight(), 0xAAAAAA);
        }
        Renderer.R2D.renderRoundedQuad(stack1,
                new Color(60, 60, 60, 255),
                actual.x - width / 2d + 2,
                actual.y + labelHeight - 2 - healthHeight,
                actual.x + width / 2d - 2,
                actual.y + labelHeight - 2,
                healthHeight / 2d,
                10
        );
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healthPer = health / maxHealth;
        healthPer = MathHelper.clamp(healthPer, 0, 1);
        double drawTo = MathHelper.lerp(healthPer, actual.x - width / 2d + 2 + healthHeight, actual.x + width / 2d - 2);
        Color MID_END = Renderer.Util.lerp(TargetHud.GREEN, TargetHud.RED, healthPer);
        Renderer.R2D.renderRoundedQuad(stack1,
                MID_END,
                actual.x - width / 2d + 2,
                actual.y + labelHeight - 2 - healthHeight,
                drawTo,
                actual.y + labelHeight - 2,
                healthHeight / 2d,
                10
        );
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
        // sort the entire thing based on the most distant to the least distant because thats how rendering works
        for (AbstractClientPlayerEntity player : client.world.getPlayers()
                .stream()
                .sorted(Comparator.comparingDouble(value -> -value.getPos().distanceTo(client.gameRenderer.getCamera().getPos())))
                .filter(abstractClientPlayerEntity -> !abstractClientPlayerEntity.equals(client.player))
                .toList()) {
            //            String t = player.getEntityName();
            render(matrices, player, player.getName());
        }
    }

    @Override
    public void onHudRender() {

    }
}
