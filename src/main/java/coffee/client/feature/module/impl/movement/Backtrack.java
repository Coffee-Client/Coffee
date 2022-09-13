/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Backtrack extends Module {
    final List<PositionEntry> entries = new ArrayList<>();
    boolean committed = false;

    public Backtrack() {
        super("Backtrack", "Allows you to redo your movement if you messed up", ModuleType.MOVEMENT);
        Events.registerEventHandler(EventType.PACKET_SEND, event -> {
            if (!this.isEnabled() || committed) {
                return;
            }
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof PlayerMoveC2SPacket) {
                event.setCancelled(true);
            }
        }, 0);
    }

    boolean shouldBacktrack() {
        return InputUtil.isKeyPressed(CoffeeMain.client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) && CoffeeMain.client.currentScreen == null;
    }

    void shouldCommit() {
        boolean a = !committed && InputUtil.isKeyPressed(CoffeeMain.client.getWindow().getHandle(), GLFW.GLFW_KEY_ENTER) && CoffeeMain.client.currentScreen == null;
        if (a) {
            committed = true;
        }
    }

    @Override
    public void tick() {

    }

    void moveTo(PositionEntry e) {
        CoffeeMain.client.player.updatePosition(e.pos.x, e.pos.y, e.pos.z);
        CoffeeMain.client.player.setPitch((float) e.pitch);
        CoffeeMain.client.player.setYaw((float) e.yaw);
        CoffeeMain.client.player.setVelocity(e.vel);
    }

    @Override
    public void enable() {
        Utils.Logging.message("To backtrack, use the left alt key");
        Utils.Logging.message("To do the movement, press enter");
        Utils.Logging.message("To cancel, disable the module");
    }

    @Override
    public void disable() {
        entries.clear();
        committed = false;
        CoffeeMain.client.player.setNoGravity(false);
    }

    @Override
    public void onFastTick() {

        if (shouldBacktrack() && !committed && !entries.isEmpty()) {
            entries.remove(entries.size() - 1);
            moveTo(entries.get(entries.size() - 1));
        }
        shouldCommit();

        if (!shouldBacktrack() && !committed) {
            entries.add(new PositionEntry(Utils.getInterpolatedEntityPosition(CoffeeMain.client.player),
                CoffeeMain.client.player.getVelocity(),
                CoffeeMain.client.player.getPitch(),
                CoffeeMain.client.player.getYaw()));
        } else if (committed) {
            CoffeeMain.client.player.setNoGravity(true);
            moveTo(entries.get(0));
            entries.remove(0);
            if (entries.isEmpty()) {
                setEnabled(false);
            }
        }
        super.onFastTick();
    }

    @Override
    public String getContext() {
        return entries.size() + "";
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        for (int i = Math.max(1, entries.size() - 30); i < entries.size(); i++) {
            Renderer.R3D.renderLine(matrices, Color.RED, entries.get(i - 1).pos(), entries.get(i).pos());
        }
    }

    @Override
    public void onHudRender() {

    }

    record PositionEntry(Vec3d pos, Vec3d vel, double pitch, double yaw) {
    }
}
