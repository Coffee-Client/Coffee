/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.MouseEvent;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.util.Utils;
import coffee.client.mixin.network.PlayerInteractEntityC2SPacketMixin;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class Boom extends Module {
    final DoubleSetting speed = this.config.create(new DoubleSetting.Builder(2).name("Speed")
        .description("How fast the fireball goes")
        .min(1)
        .max(10)
        .precision(1)
        .get());
    final DoubleSetting power = this.config.create(new DoubleSetting.Builder(20).precision(0)
        .name("Power")
        .description("How big the fireball will be")
        .min(0)
        .max(127)
        .get());
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.FireballGhast).name("Mode").description("How to send the fireball off").get());
    long lastFired = 0L;

    public Boom() {
        super("Boom", "Spawns fireballs wherever you click", ModuleType.WORLD);
        Events.registerEventHandler(EventType.MOUSE_EVENT, event -> {
            if (!this.isEnabled() || CoffeeMain.client.currentScreen != null) {
                return;
            }
            MouseEvent me = (MouseEvent) event;
            if (me.getButton() == 0 && me.getAction() == 1) {
                if (mode.getValue() == Mode.FireballGhast) {
                    fbGhast();
                } else {
                    fbInstant();
                }
            }
        }, 0);
        speed.showIf(() -> mode.getValue() == Mode.FireballGhast);
        // this is basically just to prevent the double hitting the fireball
        // if we hit the fireball after we fired it (1 second time frame from fire -> hit), we just don't do it
        // we don't want to reset velocity we gave it
        Events.registerEventHandler(EventType.PACKET_SEND, event -> {
            if (!this.isEnabled()) {
                return;
            }
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof PlayerInteractEntityC2SPacket e) {
                PlayerInteractEntityC2SPacketMixin a = (PlayerInteractEntityC2SPacketMixin) e;
                Entity entity = Objects.requireNonNull(CoffeeMain.client.world).getEntityById(a.getEntityId());
                if (entity != null && entity.getType() == EntityType.FIREBALL && System.currentTimeMillis() - lastFired < 1000) {
                    event.setCancelled(true);
                }
            }
        }, 0);
    }

    void fbInstant() {
        if (!Objects.requireNonNull(CoffeeMain.client.interactionManager).hasCreativeInventory()) {
            return;
        }
        HitResult hr = Objects.requireNonNull(CoffeeMain.client.player).raycast(200, 0, false);
        Vec3d n = hr.getPos();
        String nbt = String.format("{EntityTag:{id:\"minecraft:fireball\",ExplosionPower:%db,Motion:[%sd,%sd,%sd],Pos:[%s,%s,%s],Item:{id:\"minecraft:egg\",Count:1b}}}",
            ((int) Math.floor(power.getValue())),
            0,
            -2,
            0,
            n.getX(),
            n.getY(),
            n.getZ());
        ItemStack stack = Utils.generateItemStackWithMeta(nbt, Items.BAT_SPAWN_EGG);
        ItemStack air = CoffeeMain.client.player.getInventory().getMainHandStack().copy();
        Vec3d a = CoffeeMain.client.player.getEyePos();
        BlockHitResult bhr = new BlockHitResult(a, Direction.DOWN, new BlockPos(a), false);
        CreativeInventoryActionC2SPacket u1 = new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(CoffeeMain.client.player.getInventory().selectedSlot),
            stack);
        CreativeInventoryActionC2SPacket u2 = new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(CoffeeMain.client.player.getInventory().selectedSlot),
            air);
        PlayerInteractBlockC2SPacket p1 = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world));
        Objects.requireNonNull(CoffeeMain.client.getNetworkHandler()).sendPacket(u1);
        CoffeeMain.client.getNetworkHandler().sendPacket(p1);
        CoffeeMain.client.getNetworkHandler().sendPacket(u2);
        lastFired = System.currentTimeMillis();
    }

    void fbGhast() {
        if (!Objects.requireNonNull(CoffeeMain.client.interactionManager).hasCreativeInventory()) {
            return;
        }
        Vec3d v = Objects.requireNonNull(CoffeeMain.client.player).getRotationVector();
        v = v.multiply(speed.getValue() / 10d);
        String nbt = String.format("{EntityTag:{id:\"minecraft:fireball\",ExplosionPower:%db,power:[%s,%s,%s],Item:{id:\"minecraft:egg\",Count:1b}}}",
            ((int) Math.floor(power.getValue())),
            v.x,
            v.y,
            v.z);
        ItemStack stack = Utils.generateItemStackWithMeta(nbt, Items.BAT_SPAWN_EGG);
        ItemStack air = CoffeeMain.client.player.getInventory().getMainHandStack().copy();
        Vec3d a = CoffeeMain.client.player.getEyePos();
        BlockHitResult bhr = new BlockHitResult(a, Direction.DOWN, new BlockPos(a), false);
        CreativeInventoryActionC2SPacket u1 = new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(CoffeeMain.client.player.getInventory().selectedSlot),
            stack);
        CreativeInventoryActionC2SPacket u2 = new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(CoffeeMain.client.player.getInventory().selectedSlot),
            air);
        PlayerInteractBlockC2SPacket p1 = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world));
        Objects.requireNonNull(CoffeeMain.client.getNetworkHandler()).sendPacket(u1);
        CoffeeMain.client.getNetworkHandler().sendPacket(p1);
        CoffeeMain.client.getNetworkHandler().sendPacket(u2);
        lastFired = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        if (!Objects.requireNonNull(CoffeeMain.client.interactionManager).hasCreativeInventory()) {
            Notification.create(6000, "", true, Notification.Type.INFO, "You need to be in creative");
            setEnabled(false);
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
        return ((int) (this.power.getValue() + 0)) + "!".repeat((int) Math.floor(this.power.getValue() / 20d));
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    public enum Mode {
        FireballGhast, FireballInstant
    }
}
