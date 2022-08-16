/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.combat;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;

import java.util.Objects;

public class ShulkerDeflector extends Module {
    final BooleanSetting checkOwner = this.config.create(new BooleanSetting.Builder(true).name("Check owner").description("Check if you own the projectile, else hit it").get());

    public ShulkerDeflector() {
        super("ShulkerDeflector", "Automatically reflects shulker's projectiles", ModuleType.COMBAT);
    }

    @Override
    public void tick() {

    }

    boolean inHitRange(Entity attacker, Entity target) {
        return attacker.getCameraPosVec(1f).distanceTo(target.getPos().add(0, target.getHeight() / 2, 0)) <= Objects.requireNonNull(CoffeeMain.client.interactionManager).getReachDistance();
    }

    @Override
    public void onFastTick() {
        for (Entity entity : Objects.requireNonNull(CoffeeMain.client.world).getEntities()) {
            if (entity instanceof ShulkerBulletEntity sbe && inHitRange(Objects.requireNonNull(CoffeeMain.client.player), sbe)) {
                if (checkOwner.getValue() && sbe.getOwner() != null && sbe.getOwner().equals(CoffeeMain.client.player)) {
                    continue;
                }
                Objects.requireNonNull(CoffeeMain.client.interactionManager).attackEntity(CoffeeMain.client.player, sbe);
            }
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

    }
}
