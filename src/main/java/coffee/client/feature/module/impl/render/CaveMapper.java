/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.feature.module.impl.world.XRAY;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CaveMapper extends Module {

    final Map<Block, Color> oreColors = new HashMap<>();
    final List<BlockPos> scannedBlocks = new ArrayList<>();
    final List<BlockPos> ores = new ArrayList<>();
    final List<BlockPos> toScan = new ArrayList<>();
    final List<Map.Entry<BlockPos, List<Vec3d>>> circ = new ArrayList<>();
    final BooleanSetting coal = this.config.create(new BooleanSetting.Builder(false).name("Coal").description("Whether to show coal").get());
    final BooleanSetting iron = this.config.create(new BooleanSetting.Builder(false).name("Iron").description("Whether to show iron").get());
    final BooleanSetting gold = this.config.create(new BooleanSetting.Builder(false).name("Gold").description("Whether to show gold").get());
    final BooleanSetting redstone = this.config.create(new BooleanSetting.Builder(false).name("Redstone").description("Whether to show redstone ore").get());
    final BooleanSetting diamond = this.config.create(new BooleanSetting.Builder(true).name("Diamond").description("Whether to show diamonds").get());
    final BooleanSetting lapis = this.config.create(new BooleanSetting.Builder(false).name("Lapis").description("Whether to show lapis").get());
    final BooleanSetting copper = this.config.create(new BooleanSetting.Builder(false).name("Copper").description("Whether to show copper").get());
    final BooleanSetting emerald = this.config.create(new BooleanSetting.Builder(false).name("Emerald").description("Whether to show emeralds").get());
    final BooleanSetting quartz = this.config.create(new BooleanSetting.Builder(false).name("Quartz").description("Whether to show quartz").get());
    final BooleanSetting debris = this.config.create(new BooleanSetting.Builder(true).name("Ancient debris")
            .description("Whether to show ancient debris")
            .get());
    final BooleanSetting showScanned = this.config.create(new BooleanSetting.Builder(true).name("Show scanned")
            .description("Whether to show the scanned area")
            .get());
    final BooleanSetting showEntire = this.config.create(new BooleanSetting.Builder(false).name("Show entire area")
            .description("Whether to show the entire scanned area (VERY performance intensive)")
            .get());
    final DoubleSetting cacheSize = this.config.create(new DoubleSetting.Builder(10000).precision(0)
            .name("Cache size")
            .description("How big the cache should be (bigger = more time + more memory)")
            .min(5000)
            .max(30000)
            .get());
    final BooleanSetting includeTranslucent = this.config.create(new BooleanSetting.Builder(true).name("Scan transparent")
            .description("Scan through transparent blocks as well")
            .get());
    BlockPos start = null;
    boolean scanned = false;

    public CaveMapper() {
        super("CaveMapper", "Maps a cave for ores, scanning for exposed ones, to bypass antixray plugins", ModuleType.RENDER);
        oreColors.put(Blocks.COAL_ORE, new Color(47, 44, 54));
        oreColors.put(Blocks.IRON_ORE, new Color(236, 173, 119));
        oreColors.put(Blocks.GOLD_ORE, new Color(247, 229, 30));
        oreColors.put(Blocks.REDSTONE_ORE, new Color(245, 7, 23));
        oreColors.put(Blocks.DIAMOND_ORE, new Color(33, 244, 255));
        oreColors.put(Blocks.LAPIS_ORE, new Color(8, 26, 189));
        oreColors.put(Blocks.COPPER_ORE, new Color(239, 151, 0));
        oreColors.put(Blocks.EMERALD_ORE, new Color(27, 209, 45));
        oreColors.put(Blocks.NETHER_GOLD_ORE, new Color(247, 229, 30));
        oreColors.put(Blocks.NETHER_QUARTZ_ORE, new Color(205, 205, 205));
        oreColors.put(Blocks.ANCIENT_DEBRIS, new Color(209, 27, 245));
        oreColors.put(Blocks.DEEPSLATE_COAL_ORE, new Color(47, 44, 54));
        oreColors.put(Blocks.DEEPSLATE_IRON_ORE, new Color(236, 173, 119));
        oreColors.put(Blocks.DEEPSLATE_GOLD_ORE, new Color(247, 229, 30));
        oreColors.put(Blocks.DEEPSLATE_REDSTONE_ORE, new Color(245, 7, 23));
        oreColors.put(Blocks.DEEPSLATE_DIAMOND_ORE, new Color(33, 244, 255));
        oreColors.put(Blocks.DEEPSLATE_LAPIS_ORE, new Color(8, 26, 189));
        oreColors.put(Blocks.DEEPSLATE_COPPER_ORE, new Color(239, 151, 0));
        oreColors.put(Blocks.DEEPSLATE_EMERALD_ORE, new Color(27, 209, 45));
    }

    @Override
    public void onFastTick() {
        for (int i = 0; i < 10; i++) {
            if (scannedBlocks.size() > cacheSize.getValue() || toScan.isEmpty()) {
                toScan.clear();
                //hits.clear();
                if (!scanned) {
                    Notification.create(6000, "CaveMapper", false, Notification.Type.SUCCESS, "done scanning");
                }
                scanned = true;
                return;
            }
            BlockPos blockPos = toScan.get(0);
            toScan.remove(blockPos);
            BlockPos right = blockPos.add(1, 0, 0);
            BlockPos left = blockPos.add(-1, 0, 0);
            BlockPos fw = blockPos.add(0, 0, 1);
            BlockPos bw = blockPos.add(0, 0, -1);
            BlockPos up = blockPos.add(0, 1, 0);
            BlockPos down = blockPos.add(0, -1, 0);
            for (BlockPos pos : new BlockPos[] { right, left, fw, bw, up, down }) {
                boolean hadObstacle = false;
                int y = pos.getY();
                while (!Objects.requireNonNull(CoffeeMain.client.world).isOutOfHeightLimit(y)) {
                    BlockPos current = new BlockPos(pos.getX(), y, pos.getZ());
                    if (!bs(current).isAir()) {
                        hadObstacle = true;
                        break;
                    }
                    y++;
                }
                if (hadObstacle && (bs(pos).isAir() || (includeTranslucent.getValue() && !bs(pos).getMaterial().blocksLight()))) {
                    if (!scannedBlocks.contains(pos)) {
                        toScan.add(pos);
                        scannedBlocks.add(pos);
                    }
                } else if (bs(pos).isFullCube(CoffeeMain.client.world, pos) && circ.stream()
                        .noneMatch(blockPosListEntry -> blockPosListEntry.getKey().equals(pos))) {
                    Vec3d renderR = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                    Vec3d end = renderR.add(new Vec3d(1, 1, 1));
                    float x1 = (float) renderR.x;
                    float y1 = (float) renderR.y;
                    float z1 = (float) renderR.z;
                    float x2 = (float) end.x;
                    float y2 = (float) end.y;
                    float z2 = (float) end.z;
                    List<Vec3d> vertecies = new ArrayList<>();
                    float offset = 0.005f;
                    if (bs(pos.add(0, 1, 0)).isAir()) {
                        vertecies.add(new Vec3d(x1, y2 + offset, z1));
                        vertecies.add(new Vec3d(x1, y2 + offset, z2));

                        vertecies.add(new Vec3d(x1, y2 + offset, z2));
                        vertecies.add(new Vec3d(x2, y2 + offset, z2));

                        vertecies.add(new Vec3d(x2, y2 + offset, z2));
                        vertecies.add(new Vec3d(x2, y2 + offset, z1));

                        vertecies.add(new Vec3d(x2, y2 + offset, z1));
                        vertecies.add(new Vec3d(x1, y2 + offset, z1));
                    }

                    // right
                    if (bs(pos.add(0, 0, 1)).isAir()) {
                        vertecies.add(new Vec3d(x1, y1, z2 + offset));
                        vertecies.add(new Vec3d(x2, y1, z2 + offset));

                        vertecies.add(new Vec3d(x2, y1, z2 + offset));
                        vertecies.add(new Vec3d(x2, y2, z2 + offset));

                        vertecies.add(new Vec3d(x2, y2, z2 + offset));
                        vertecies.add(new Vec3d(x1, y2, z2 + offset));

                        vertecies.add(new Vec3d(x1, y2, z2 + offset));
                        vertecies.add(new Vec3d(x1, y1, z2 + offset));
                    }

                    // front
                    if (bs(pos.add(1, 0, 0)).isAir()) {
                        vertecies.add(new Vec3d(x2 + offset, y2, z2));
                        vertecies.add(new Vec3d(x2 + offset, y1, z2));

                        vertecies.add(new Vec3d(x2 + offset, y1, z2));
                        vertecies.add(new Vec3d(x2 + offset, y1, z1));

                        vertecies.add(new Vec3d(x2 + offset, y1, z1));
                        vertecies.add(new Vec3d(x2 + offset, y2, z1));

                        vertecies.add(new Vec3d(x2 + offset, y2, z1));
                        vertecies.add(new Vec3d(x2 + offset, y2, z2));
                    }

                    // left
                    if (bs(pos.add(0, 0, -1)).isAir()) {
                        vertecies.add(new Vec3d(x2, y2, z1 - offset));
                        vertecies.add(new Vec3d(x2, y1, z1 - offset));

                        vertecies.add(new Vec3d(x2, y1, z1 - offset));
                        vertecies.add(new Vec3d(x1, y1, z1 - offset));

                        vertecies.add(new Vec3d(x1, y1, z1 - offset));
                        vertecies.add(new Vec3d(x1, y2, z1 - offset));

                        vertecies.add(new Vec3d(x1, y2, z1 - offset));
                        vertecies.add(new Vec3d(x2, y2, z1 - offset));
                    }

                    // back
                    if (bs(pos.add(-1, 0, 0)).isAir()) {
                        vertecies.add(new Vec3d(x1 - offset, y2, z1));
                        vertecies.add(new Vec3d(x1 - offset, y1, z1));

                        vertecies.add(new Vec3d(x1 - offset, y1, z1));
                        vertecies.add(new Vec3d(x1 - offset, y1, z2));

                        vertecies.add(new Vec3d(x1 - offset, y1, z2));
                        vertecies.add(new Vec3d(x1 - offset, y2, z2));

                        vertecies.add(new Vec3d(x1 - offset, y2, z2));
                        vertecies.add(new Vec3d(x1 - offset, y2, z1));
                    }

                    // down
                    if (bs(pos.add(0, -1, 0)).isAir()) {
                        vertecies.add(new Vec3d(x1, y1 - offset, z1));
                        vertecies.add(new Vec3d(x2, y1 - offset, z1));

                        vertecies.add(new Vec3d(x2, y1 - offset, z1));
                        vertecies.add(new Vec3d(x2, y1 - offset, z2));

                        vertecies.add(new Vec3d(x2, y1 - offset, z2));
                        vertecies.add(new Vec3d(x1, y1 - offset, z2));

                        vertecies.add(new Vec3d(x1, y1 - offset, z2));
                        vertecies.add(new Vec3d(x1, y1 - offset, z1));
                    }
                    Map.Entry<BlockPos, List<Vec3d>> e = new AbstractMap.SimpleEntry<>(pos, vertecies);
                    circ.add(e);
                }
                if (XRAY.blocks.contains(bs(pos).getBlock())) {
                    if (!ores.contains(pos)) {
                        ores.add(pos);
                    }
                }
            }
        }
    }

    boolean shouldRenderOre(Block b) {
        return (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE) && coal.getValue() || (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE) && iron.getValue() || (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE) && gold.getValue() || (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE) && redstone.getValue() || (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE) && diamond.getValue() || (b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE) && lapis.getValue() || (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE) && copper.getValue() || (b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE) && emerald.getValue() || b == Blocks.NETHER_QUARTZ_ORE && quartz.getValue() || b == Blocks.ANCIENT_DEBRIS && debris.getValue();

    }

    BlockState bs(BlockPos bp) {
        return Objects.requireNonNull(CoffeeMain.client.world).getBlockState(bp);
    }

    @Override
    public void tick() {
    }

    @Override
    public void enable() {
        scannedBlocks.clear();
        toScan.clear();
        ores.clear();
        circ.clear();
        start = Objects.requireNonNull(CoffeeMain.client.player).getBlockPos();
        toScan.add(start);
        scanned = false;
    }

    @Override
    public String getContext() {
        return scannedBlocks.size() + "S|" + new ArrayList<>(this.ores).stream()
                .filter(blockPos -> shouldRenderOre(Objects.requireNonNull(CoffeeMain.client.world).getBlockState(blockPos).getBlock()))
                .count() + "F|" + Utils.Math.roundToDecimal((double) new ArrayList<>(this.ores).stream()
                .filter(blockPos -> shouldRenderOre(Objects.requireNonNull(CoffeeMain.client.world).getBlockState(blockPos).getBlock()))
                .count() / scannedBlocks.size() * 100, 2) + "%D";
    }

    @Override
    public void disable() {
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        for (BlockPos hit : new ArrayList<>(toScan)) {
            if (hit == null) {
                continue;
            }
            Renderer.R3D.renderOutline(new Vec3d(hit.getX(), hit.getY(), hit.getZ()), new Vec3d(1, 1, 1), Color.WHITE, matrices);
        }
        List<Map.Entry<BlockPos, List<Vec3d>>> real = new ArrayList<>(circ);

        Camera cam = CoffeeMain.client.gameRenderer.getCamera();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        if (showScanned.getValue()) {
            for (int i = 0; i < real.size(); i++) {
                Map.Entry<BlockPos, List<Vec3d>> entry = real.get(i);
                if (entry == null) {
                    continue;
                }
                if (ores.contains(entry.getKey())) {
                    continue;
                }
                double dist = new Vec3d(entry.getKey().getX(),
                        entry.getKey().getY(),
                        entry.getKey().getZ()
                ).distanceTo(Objects.requireNonNull(CoffeeMain.client.player)
                        .getPos());
                dist = (1 - MathHelper.clamp(dist, 0, 15) / 15d) * 3d;
                dist = Math.round(dist);
                dist /= 3;
                if (showEntire.getValue()) {
                    dist = 1;
                }
                float p = (float) i / real.size();
                double s = (System.currentTimeMillis() % 3000) / (double) 3000;
                p += s;
                p %= 1;
                Color c = Renderer.Util.modify(Color.getHSBColor(p, 0.5f, 1f), -1, -1, -1, (int) (dist * 255));
                {
                    float red = c.getRed() / 255f;
                    float green = c.getGreen() / 255f;
                    float blue = c.getBlue() / 255f;
                    float alpha = c.getAlpha() / 255f;
                    Vec3d camPos = cam.getPos();

                    for (Vec3d vec3d : entry.getValue()) {
                        Vec3d pp = vec3d.subtract(camPos);
                        buffer.vertex(matrix, (float) pp.x, (float) pp.y, (float) pp.z).color(red, green, blue, alpha).next();
                    }
                }
            }
        }
        BufferRenderer.drawWithShader(buffer.end());
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        for (BlockPos ore : new ArrayList<>(this.ores)) {
            if (ore == null) {
                continue;
            }
            Block t = Objects.requireNonNull(CoffeeMain.client.world).getBlockState(ore).getBlock();
            if (!shouldRenderOre(t)) {
                continue;
            }
            Vec3d p = new Vec3d(ore.getX(), ore.getY(), ore.getZ());
            double dist = p.distanceTo(Objects.requireNonNull(CoffeeMain.client.player).getPos());
            dist = MathHelper.clamp(dist, 0, 30);
            Renderer.R3D.renderFilled(p,
                    new Vec3d(1, 1, 1),
                    Renderer.Util.modify(oreColors.containsKey(t) ? oreColors.get(t) : new Color(CoffeeMain.client.world.getBlockState(ore)
                            .getMapColor(CoffeeMain.client.world, ore).color), -1, -1, -1, (int) ((dist / 30d) * 200)),
                    matrices
            );
        }
    }

    @Override
    public void onHudRender() {
    }
}
