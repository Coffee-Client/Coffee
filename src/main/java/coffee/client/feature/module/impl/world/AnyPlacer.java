/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.MouseEvent;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class AnyPlacer extends Module {
    @Setting(name = "Height offset", description = "Offsets the placed entity in the Y direction", min = -5, max = 5, precision = 1)
    double heightOffset = 1;

    public AnyPlacer() {
        super("AnyPlacer", "Places spawn eggs with infinite reach (requires creative)", ModuleType.WORLD);
        Events.registerEventHandler(EventType.MOUSE_EVENT, event -> {
            if (!this.isEnabled()) {
                return;
            }
            if (CoffeeMain.client.player == null || CoffeeMain.client.world == null) {
                return;
            }
            if (CoffeeMain.client.currentScreen != null) {
                return;
            }
            MouseEvent me = (MouseEvent) event;
            if ((me.getAction() == 1 || me.getAction() == 2) && me.getButton() == 1) {
                ItemStack sex = CoffeeMain.client.player.getMainHandStack();
                if (sex.getItem() instanceof SpawnEggItem) {
                    event.setCancelled(true);
                    Vec3d rotationVector = Rotations.getRotationVector(Rotations.getClientPitch(), Rotations.getClientYaw());
                    EntityHitResult raycast = ProjectileUtil.raycast(client.player, CoffeeMain.client.player.getCameraPosVec(0), CoffeeMain.client.player.getCameraPosVec(0).add(rotationVector.multiply(500)), client.player.getBoundingBox().stretch(rotationVector.multiply(500)).expand(1, 1, 1), Entity::isAttackable, 500 * 500);
                    Vec3d spawnPos;
                    if (raycast != null && raycast.getEntity() != null) {
                        spawnPos = raycast.getPos();
                    } else {
                        HitResult hr = CoffeeMain.client.player.raycast(500, 0, true);
                        spawnPos = hr.getPos();
                    }
                    spawnPos = spawnPos.add(0, heightOffset, 0);
                    NbtCompound entityTag = sex.getOrCreateSubNbt("EntityTag");
                    NbtList nl = new NbtList();
                    nl.add(NbtDouble.of(spawnPos.x));
                    nl.add(NbtDouble.of(spawnPos.y));
                    nl.add(NbtDouble.of(spawnPos.z));
                    entityTag.put("Pos", nl);
                    CreativeInventoryActionC2SPacket a = new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(CoffeeMain.client.player.getInventory().selectedSlot), sex);
                    Objects.requireNonNull(CoffeeMain.client.getNetworkHandler()).sendPacket(a);
                    BlockHitResult bhr = new BlockHitResult(CoffeeMain.client.player.getPos(), Direction.DOWN, new BlockPos(CoffeeMain.client.player.getPos()), false);
                    PlayerInteractBlockC2SPacket ib = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world));
                    CoffeeMain.client.getNetworkHandler().sendPacket(ib);
                }
            }
        }, 0);
    }

    @Override
    public void tick() {
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

    }
}
