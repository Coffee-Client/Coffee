/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import coffee.client.mixin.IPlayerListEntryMixin;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Test extends Module {

    public Test() {
        super("Test", "Testing stuff with the client, can be ignored", ModuleType.MISC);
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
    String fmt(Vec3d d) {
        return String.format(Locale.ENGLISH, "(%.4f, %.4f, %.4f)", d.x, d.y, d.z);
    }
    double getConfidence(PlayerEntity e) {
        String entityName = e.getEntityName();
        boolean allLowerCase = StringUtils.isAllLowerCase(entityName);
        PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(e.getUuid());
        boolean hasDefaultSkin = playerListEntry != null && ((IPlayerListEntryMixin) playerListEntry).coffee_getTextures().get(MinecraftProfileTexture.Type.SKIN) == null;
        boolean sprinting = e.isSprinting();
        boolean hasZeroVel = e.getVelocity().equals(Vec3d.ZERO);
        return
            (allLowerCase?1:0)
            +(hasDefaultSkin?1:0)
            +(sprinting?1:0)
            +(hasZeroVel?1:0);
    }
    @Override
    public void onWorldRender(MatrixStack matrices) {
        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
//            if (player.equals(client.player)) continue;
            Vec3d pos = Utils.getInterpolatedEntityPosition(player).add(0,player.getStandingEyeHeight(),0);
            Vec3d screenSpace = Renderer.R2D.getScreenSpaceCoordinate(pos, matrices);
            if (Renderer.R2D.isOnScreen(screenSpace)) {
                Utils.TickManager.runOnNextRender(() -> {
                    Map<String, Object> debugInfos = new HashMap<>();
                    debugInfos.put("id", player.getId());
                    debugInfos.put("onground", player.isOnGround());
//                    debugInfos.put("airtime", player.fallDistance);
//                    debugInfos.put("velocity", fmt(player.getVelocity()));
                    debugInfos.put("pos diff", fmt(player.getPos().subtract(
                        player.prevX, player.prevY, player.prevZ
                    )));
                    debugInfos.put("sprinting", player.isSprinting());
                    debugInfos.put("confidence", getConfidence(player));
                    double longest = 0;
                    for (String s : debugInfos.keySet()) {
                        longest = Math.max(FontRenderers.getMono().getStringWidth(s), longest);
                    }
                    double yOff = 0;
                    MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
                    for (String s : debugInfos.keySet()) {
                        FontRenderers.getMono().drawString(ms,s,screenSpace.x,screenSpace.y+yOff,0xCCCCCC);
                        FontRenderers.getMono().drawString(ms,debugInfos.get(s).toString(),screenSpace.x+longest+3,screenSpace.y+yOff,0xFFFFFF);
                        yOff += FontRenderers.getMono().getFontHeight();
                    }
                });
            }
        }
    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void tick() {

    }
}
