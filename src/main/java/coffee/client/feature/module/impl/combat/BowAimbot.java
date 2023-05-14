/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.combat;

import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.Rotation;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.Comparator;
import java.util.Optional;

public class BowAimbot extends Module {

    static final Color CAN_ROTATE_INNER = new Color(21, 236, 57, 50);
    static final Color CAN_ROTATE_OUTER = new Color(8, 93, 21);
    static final Color CANT_ROTATE_INNER = new Color(217, 52, 30, 50);
    static final Color CANT_ROTATE_OUTER = new Color(94, 18, 12);
    @Setting(name = "Max range", description = "How far to cap off entities for target selection", min = 10, max = 200, precision = 0)
    double maxRange = 60;
    @Setting(name = "Priority", description = "How to select an entity")
    TargetMode targetMode = TargetMode.NearestCrosshair;
    LivingEntity target;

    public BowAimbot() {
        super("BowAimbot", "Automatically aims your bow at nearby entities", ModuleType.COMBAT);
    }

    private static Rotation getRotationFor(Vec3d target) {
        float velocity = BowItem.getPullProgress(client.player.getItemUseTime());

        Vec3d cameraPosVec = client.player.getCameraPosVec(client.getTickDelta());

        double xDiff = target.getX() - cameraPosVec.getX();
        double yDiff = target.getY() - cameraPosVec.getY();
        double zDiff = target.getZ() - cameraPosVec.getZ();

        double xzDist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double xzDistSq = Math.pow(xzDist, 2);

        float velocitySq = (float) Math.pow(velocity, 2);
        float velocityQ = (float) Math.pow(velocity, 4);

        // what the fuck?
        double sqrt = Math.sqrt(velocityQ - (xzDistSq / 180 + 2 * yDiff * velocitySq) / 180);
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - sqrt) / (xzDist / 180)));

        if (!Float.isNaN(pitch)) {
            return new Rotation(pitch, Rotations.getPitchYaw(target).getYaw());
        } else {
            return null;
        }
    }

    private double computePriority(MatrixStack renderStack, LivingEntity e) {
        Vec3d interpolatedEntityPosition = Utils.getInterpolatedEntityPosition(e);
        return switch (targetMode) {
            case Nearest -> -interpolatedEntityPosition.distanceTo(client.player.getCameraPosVec(client.getTickDelta()));
            case Farthest -> interpolatedEntityPosition.distanceTo(client.player.getCameraPosVec(client.getTickDelta()));
            case NearestCrosshair -> {
                Vec3d screenSpaceCoordinate = Renderer.R2D.getScreenSpaceCoordinate(interpolatedEntityPosition.add(0, e.getHeight() * 0.5, 0), renderStack);
                if (!Renderer.R2D.isOnScreen(screenSpaceCoordinate)) {
                    yield -9999;
                } else {
                    float centerX = client.getWindow().getScaledWidth() / 2f;
                    float centerY = client.getWindow().getScaledHeight() / 2f;
                    float diffX = (float) (centerX - screenSpaceCoordinate.x);
                    float diffY = (float) (centerY - screenSpaceCoordinate.y);
                    yield -Math.sqrt(diffX * diffX + diffY * diffY);
                }
            }
        };
    }

    private LivingEntity select(MatrixStack renderStack) {
        Optional<LivingEntity> max = Utils.findEntities(livingEntity -> livingEntity.isAttackable() && livingEntity.isAlive() && livingEntity.getPos()
                                                                                                                                             .distanceTo(client.player.getCameraPosVec(client.getTickDelta())) <
                                                                                                                                 maxRange)
                                          .max(Comparator.comparingDouble(value -> computePriority(renderStack, value)));
        return max.orElse(null);
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

    private boolean shouldAim() {
        ItemStack mainHandStack = client.player.getMainHandStack();
        Item item = mainHandStack.getItem();
        return item == Items.BOW && client.player.isUsingItem() && BowItem.getPullProgress(client.player.getItemUseTime()) >= 0.1;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (!shouldAim()) {
            this.target = null;
            return;
        }

        this.target = select(matrices);
        if (this.target != null) {
            Rotation rotationFor = getRotationFor(this.target.getPos().add(0, this.target.getHeight() * 0.5, 0));
            boolean canRotate = rotationFor != null;
            if (canRotate) {
                Rotations.setClientRotation(rotationFor);
            }
            Vec3d interpolatedEntityPosition = Utils.getInterpolatedEntityPosition(this.target);
            float width = this.target.getWidth();
            double halfWidth = width * 0.5;
            Vec3d start = interpolatedEntityPosition.subtract(halfWidth, 0, halfWidth);
            Vec3d dim = new Vec3d(width, this.target.getHeight(), width);
            Renderer.R3D.renderEdged(matrices, canRotate ? CAN_ROTATE_INNER : CANT_ROTATE_INNER, canRotate ? CAN_ROTATE_OUTER : CANT_ROTATE_OUTER, start, dim);
        }
    }

    @Override
    public void onHudRender() {

    }

    public enum TargetMode {
        Nearest, Farthest, NearestCrosshair
    }
}
