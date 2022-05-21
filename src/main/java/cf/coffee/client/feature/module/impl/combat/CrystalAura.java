package cf.coffee.client.feature.module.impl.combat;

import cf.coffee.client.feature.config.annotation.Setting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrystalAura extends Module {
    @Setting(name = "Through walls", description = "Places crystals through walls")
    boolean throughWalls = false;
    @Setting(name = "Range", description = "How far to target entities from", min = 3, max = 16, precision = 1)
    double range = 10;
    List<EntityEntry> targets = new CopyOnWriteArrayList<>();
    List<BlockPos> obsidianPositions = new CopyOnWriteArrayList<>();
    Thread obsidianUpdater = new Thread(() -> {
        while (true) {
            Utils.sleep(30);
            if (!this.isEnabled() || client.world == null || client.player == null) continue;
            updateObsidian();
        }
    });

    public CrystalAura() {
        super("CrystalAura", "Test", ModuleType.MISC);
        obsidianUpdater.start();
    }

    boolean isABObstructed(Vec3d a, Vec3d b) {
        return Utils.Math.isABObstructed(a, b, client.world, client.player);
    }

    void updateObsidian() {
        obsidianPositions.clear();
        Vec3d ppos = client.player.getPos();
        float reachDist = client.interactionManager.getReachDistance();
        int reach = (int) Math.floor(reachDist);
        for (int xOff = -reach; xOff < reach; xOff++) {
            for (int zOff = -reach; zOff < reach; zOff++) {
                for (int yOff = reach; yOff > -reach; yOff--) {
                    Vec3d newPos = ppos.add(xOff, yOff, zOff);
                    if (newPos.distanceTo(ppos) > reach) continue;
                    BlockPos t = new BlockPos(newPos);
                    Block b = client.world.getBlockState(t).getBlock();
                    if ((b == Blocks.BEDROCK || b == Blocks.OBSIDIAN) && client.world.getBlockState(t.up()).isAir()) {
                        if (!throughWalls && isABObstructed(Vec3d.of(t).add(.5, 1, .5), client.player.getEyePos()))
                            continue;
                        obsidianPositions.add(t);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        targets.clear();
        for (Entity entity : client.world.getEntities()) {
            if (entity.equals(client.player)) continue;
            if (entity.getPos().distanceTo(client.player.getEyePos()) < range) {
                List<Vec3d> potentialTargets = new ArrayList<>();
                for (BlockPos obsidianPosition : obsidianPositions) {
                    Vec3d center = Vec3d.of(obsidianPosition);
                    if (!isABObstructed(center.add(.5, 1, .5), entity.getPos()) || !isABObstructed(center.add(.5, 1, .5), entity.getPos()
                            .add(0, entity.getHeight(), 0))) potentialTargets.add(center);
                }
                Vec3d t = potentialTargets.stream()
                        .min(Comparator.comparingDouble(value -> value.distanceTo(entity.getPos())))
                        .orElse(null);
                if (t == null) continue;
                targets.add(new EntityEntry(entity, new BlockPos(t)));
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
        for (EntityEntry target : targets) {
            Renderer.R3D.renderLine(target.target().getPos(), target.target().getEyePos(), Color.WHITE, matrices);
            if (target.freeAB() != null) {
                Renderer.R3D.renderLine(Vec3d.of(target.freeAB()).add(.5, 1, .5), target.target()
                        .getPos(), Color.RED, matrices);
                Renderer.R3D.renderFadingBlock(Color.WHITE, new Color(255, 50, 50, 100), Vec3d.of(target.freeAB()), new Vec3d(1, 1, 1), 1000);
            }
        }
    }

    @Override
    public void onHudRender() {

    }

    record EntityEntry(Entity target, BlockPos freeAB) {
    }
}
