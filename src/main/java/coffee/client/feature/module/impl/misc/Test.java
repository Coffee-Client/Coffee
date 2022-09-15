/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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

    Entity target;

    @Override
    public void onWorldRender(MatrixStack matrices) {
        double range = 200;
        Vec3d ranged = Rotations.getRotationVector(Rotations.getClientPitch(), Rotations.getClientYaw()).multiply(range);
        Box allowed = client.player.getBoundingBox().stretch(ranged).expand(1, 1, 1);
        Vec3d cameraPosVec1 = CoffeeMain.client.player.getCameraPosVec(client.getTickDelta());
        EntityHitResult ehr = ProjectileUtil.raycast(CoffeeMain.client.player, cameraPosVec1, cameraPosVec1.add(ranged), allowed, Entity::isAttackable, range * range);
        if (ehr != null && ehr.getEntity() != null) {
            target = ehr.getEntity();
        }
        if (target == null) {
            return;
        }

        float velocity = BowItem.getPullProgress(client.player.getItemUseTime());

        Vec3d interpolatedEntityPosition = Utils.getInterpolatedEntityPosition(target);
        Vec3d interpolatedEntityPosition1 = Utils.getInterpolatedEntityPosition(client.player);
        Vec3d cameraPosVec = client.player.getCameraPosVec(client.getTickDelta());

        double xDiff = interpolatedEntityPosition.getX() - interpolatedEntityPosition1.getX();
        double yDiff = interpolatedEntityPosition.y + target.getHeight() / 2 - cameraPosVec.y;
        double zDiff = interpolatedEntityPosition.getZ() - interpolatedEntityPosition1.getZ();

        double xzDist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double xzDistSq = xzDist * xzDist;

        float velocitySq = velocity * velocity;

        double sqrt = Math.sqrt((velocitySq * velocitySq) - ((xzDistSq / 180 + 2 * yDiff * velocitySq) / 180));
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - sqrt) / (xzDist / 180)));

        if (!Float.isNaN(pitch)) {
            Utils.Logging.message(pitch + "");
            Rotations.setClientPitch(pitch);
            Rotations.setClientYaw(Rotations.getPitchYaw(interpolatedEntityPosition.add(0, target.getHeight() / 2, 0)).getYaw());
        }

    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void tick() {
        //                if (client.player.isUsingItem() && client.player.getItemUseTime() >= 3) {
        //                    client.interactionManager.stopUsingItem(client.player);
        //                }
    }
}
