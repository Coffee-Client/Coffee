/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.hud.element;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.feature.module.impl.render.Radar;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class RadarElement extends HudElement {
    public RadarElement() {
        super("Radar", 5, 100 + ModuleType.values().length * FontRenderers.getRenderer().getMarginHeight() + 4 + 5, 100, 100);
    }


    @Override
    public void renderIntern(MatrixStack stack) {
        Radar radar = ModuleRegistry.getByClass(Radar.class);
        if (radar.isEnabled()) {
            Renderer.R2D.renderRoundedQuadWithShadow(stack, new Color(10, 10, 20, 200), 0, 0, width, height, 5, 20);
            double maxDistToRender = radar.iScale * 16;
            Vec3d ppos = Utils.getInterpolatedEntityPosition(CoffeeMain.client.player);
            double originX = ppos.x;
            double originZ = ppos.z;
            ClipStack.globalInstance.addWindow(stack, new Rectangle(0, 0, width, height));
            stack.push();
            stack.translate(width / 2d, height / 2d, 0);
            stack.multiply(new Quaternion(0, 0, -CoffeeMain.client.player.getYaw() - 180, true));
            for (Entity entity : CoffeeMain.client.world.getEntities()) {
                Vec3d epos = Utils.getInterpolatedEntityPosition(entity);
                double entityX = epos.x;
                double entityZ = epos.z;
                double deltaX = entityX - originX;
                double deltaZ = entityZ - originZ;
                deltaX = deltaX / maxDistToRender;
                deltaZ = deltaZ / maxDistToRender;
                deltaX *= width;
                deltaZ *= width;
                Color c;
                if (entity.equals(CoffeeMain.client.player)) {
                    c = Color.WHITE;
                } else if (entity instanceof PlayerEntity) {
                    c = Color.RED;
                } else if (entity instanceof ItemEntity) {
                    c = Color.CYAN;
                } else if (entity instanceof EndermanEntity enderman) {
                    if (enderman.isProvoked()) {
                        c = Color.YELLOW;
                    } else {
                        c = Color.GREEN;
                    }
                } else if (entity instanceof HostileEntity) {
                    c = Color.YELLOW;
                } else {
                    c = Color.GREEN;
                }
                Renderer.R2D.renderCircle(stack, c, deltaX, deltaZ, 1, 10);
            }
            stack.pop();
            ClipStack.globalInstance.popWindow();
        }
    }
}
