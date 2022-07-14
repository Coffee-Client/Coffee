/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.combat;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Objects;


public class AutoAttack extends Module {

    public AutoAttack() {
        super("AutoAttack", "Automatically attacks the entity you're looking at", ModuleType.COMBAT);
    }

    @Override
    public void tick() {
        if (!(CoffeeMain.client.crosshairTarget instanceof EntityHitResult) || Objects.requireNonNull(CoffeeMain.client.player)
                .getAttackCooldownProgress(0) < 1) {
            return;
        }
        Objects.requireNonNull(CoffeeMain.client.interactionManager)
                .attackEntity(CoffeeMain.client.player, ((EntityHitResult) CoffeeMain.client.crosshairTarget).getEntity());
        CoffeeMain.client.player.swingHand(Hand.MAIN_HAND);
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
