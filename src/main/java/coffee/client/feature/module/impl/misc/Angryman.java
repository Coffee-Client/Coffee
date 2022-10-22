/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Timer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;

import java.util.Comparator;

public class Angryman extends Module {
    LivingEntity nextTarget;
    Timer updater = new Timer();

    public Angryman() {
        super("Angryman", "Automatically makes surrounding endermen angry, at you", ModuleType.MISC);
    }

    @Override
    public void tick() {
        if (updater.hasExpired(1000)) {
            updater.reset();
            nextTarget = Utils.findEntities(livingEntity -> livingEntity instanceof EndermanEntity e && client.player.canSee(e) && !e.isProvoked())
                .min(Comparator.comparingDouble(value -> value.distanceTo(client.player)))
                .orElse(null);
        }
        if (nextTarget != null) {
            Rotations.lookAtV3(nextTarget.getEyePos());
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
