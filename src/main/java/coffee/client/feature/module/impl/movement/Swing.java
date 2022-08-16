/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.theme.ThemeManager;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.MouseEvent;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.Objects;

public class Swing extends Module {
    static final Color line = new Color(50, 50, 50, 255);
    static BlockPos swinging;

    public Swing() {
        super("Swing", "Spiderman", ModuleType.MOVEMENT);
        Events.registerEventHandler(EventType.MOUSE_EVENT, event -> {
            if (!this.isEnabled() || CoffeeMain.client.currentScreen != null) {
                return;
            }
            MouseEvent me = (MouseEvent) event;
            if (me.getButton() == 0 && me.getAction() == 1) {
                try {
                    HitResult hit = Objects.requireNonNull(CoffeeMain.client.player).raycast(200, CoffeeMain.client.getTickDelta(), true);
                    swinging = new BlockPos(hit.getPos());
                } catch (Exception ignored) {
                }
            }
        });
        Events.registerEventHandler(EventType.PACKET_SEND, event -> {
            if (!this.isEnabled()) {
                return;
            }
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof ClientCommandC2SPacket e && e.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public void tick() {
        if (swinging == null) {
            return;
        }
        Vec3d diff = Vec3d.of(swinging).add(0.5, 0.5, 0.5).subtract(Utils.getInterpolatedEntityPosition(CoffeeMain.client.player)).normalize().multiply(0.4).add(0, 0.03999999910593033 * 2, 0);

        CoffeeMain.client.player.addVelocity(diff.x, diff.y, diff.z);
        if (CoffeeMain.client.options.sneakKey.isPressed()) {
            swinging = null;
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
        if (swinging == null || CoffeeMain.client.player == null) {
            return;
        }
        RenderSystem.defaultBlendFunc();
        Vec3d cringe = new Vec3d(swinging.getX(), swinging.getY(), swinging.getZ());
        Vec3d cringe2 = new Vec3d(swinging.getX() + 0.5, swinging.getY() + 0.5, swinging.getZ() + 0.5);
        Vec3d eSource = Utils.getInterpolatedEntityPosition(CoffeeMain.client.player);
        //        Renderer.R3D.renderFilled(cringe, new Vec3d(1, 1, 1), new Color(150, 150, 150, 150), matrices)
        Renderer.R3D.renderFilled(matrices, ThemeManager.getMainTheme().getInactive(), cringe.add(.5, .5, .5).subtract(.25, .25, .25), new Vec3d(.5, .5, .5));
        Renderer.R3D.renderLine(matrices, line, eSource, cringe2);
    }

    @Override
    public void onHudRender() {

    }
}
