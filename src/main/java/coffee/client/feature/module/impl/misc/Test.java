/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.feature.module.impl.combat.Killaura;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashMap;
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

    @Override
    public void onWorldRender(MatrixStack matrices) {
        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            Vec3d positionDiff = player.getPos().subtract(player.prevX, player.prevY, player.prevZ);
            Vec3d positionDiff1 = new Vec3d(positionDiff.x, 0, positionDiff.z).normalize();
            Vec3d interpolatedEntityPosition = Utils.getInterpolatedEntityPosition(player);
            Renderer.R3D.renderLine(matrices, Color.RED, interpolatedEntityPosition, interpolatedEntityPosition.add(positionDiff1));
            Vec3d pos = interpolatedEntityPosition.add(0, player.getStandingEyeHeight(), 0);
            Vec3d screenSpace = Renderer.R2D.getScreenSpaceCoordinate(pos, matrices);
            if (Renderer.R2D.isOnScreen(screenSpace)) {
                Utils.TickManager.runOnNextRender(() -> {
                    Map<String, Object> debugInfos = new LinkedHashMap<>();
                    debugInfos.put("matrixViolating", Arrays.toString(Killaura.Antibot.MATRIX.getViolatingChecks(player)));
                    debugInfos.put("confidence", String.format(Locale.ENGLISH, "%.2f", Killaura.Antibot.MATRIX.computeConfidence(player)));
                    debugInfos.put("spawnAndShitDistance", Killaura.playersWhoHaveSpawnedAndStayedInOurRange.getOrDefault(player.getId(), -1));
                    double longest = 0;
                    for (String s : debugInfos.keySet()) {
                        longest = Math.max(FontRenderers.getMono().getStringWidth(s), longest);
                    }
                    double yOff = 0;
                    MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
                    for (String s : debugInfos.keySet()) {
                        FontRenderers.getMono().drawString(ms, s, screenSpace.x, screenSpace.y + yOff, 0xCCCCCC);
                        FontRenderers.getMono().drawString(ms, debugInfos.get(s).toString(), screenSpace.x + longest + 3, screenSpace.y + yOff, 0xFFFFFF);
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
