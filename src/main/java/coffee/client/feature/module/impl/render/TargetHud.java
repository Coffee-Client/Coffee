/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.manager.AttackManager;
import coffee.client.helper.render.AlphaOverride;
import coffee.client.helper.render.PlayerHeadResolver;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.Texture;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL40C;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class TargetHud extends Module {

    public static final int modalWidth = 160;
    public static final int modalHeight = 42;
    static final Color GREEN = new Color(100, 255, 20);
    static final Color RED = new Color(255, 50, 20);
    final BooleanSetting renderPing = this.config.create(new BooleanSetting.Builder(true).name("Render ping").description("Shows the ping of the enemy").get());
    final BooleanSetting renderHP = this.config.create(new BooleanSetting.Builder(true).name("Render health").description("Shows the HP of the enemy").get());
    double wX = 0;
    double renderWX1 = 0;
    Entity e = null;
    Entity re = null;
    double trackedHp = 0;
    double trackedMaxHp = 0;

    public TargetHud() {
        super("TargetHud", "Shows info about your opponent", ModuleType.RENDER);
    }

    boolean isApplicable(Entity check) {
        if (check == CoffeeMain.client.player) {
            return false;
        }
        if (check.distanceTo(CoffeeMain.client.player) > 64) {
            return false;
        }
        int l = check.getEntityName().length();
        if (l < 3 || l > 16) {
            return false;
        }
        boolean isValidEntityName = Utils.Players.isPlayerNameValid(check.getEntityName());
        if (!isValidEntityName) {
            return false;
        }
        if (check == CoffeeMain.client.player) {
            return false;
        }
        return check.getType() == EntityType.PLAYER && check instanceof PlayerEntity;
    }

    @Override
    public void tick() {
        if (AttackManager.getLastAttackInTimeRange() != null) {
            e = AttackManager.getLastAttackInTimeRange();
            return;
        }
        List<Entity> entitiesQueue = StreamSupport.stream(Objects.requireNonNull(CoffeeMain.client.world).getEntities().spliterator(), false)
                .filter(this::isApplicable)
                .sorted(Comparator.comparingDouble(value -> value.getPos().distanceTo(Objects.requireNonNull(CoffeeMain.client.player).getPos())))
                .toList();
        if (entitiesQueue.size() > 0) {
            e = entitiesQueue.get(0);
        } else {
            e = null;
        }
        if (e instanceof LivingEntity ev) {
            if (ev.isDead()) {
                e = null;
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
    public void onFastTick() {
        renderWX1 = Transitions.transition(renderWX1, wX, 10);
        if (re instanceof LivingEntity e) {
            trackedHp = Transitions.transition(trackedHp, e.getHealth(), 15, 0.002);
            trackedMaxHp = Transitions.transition(trackedMaxHp, e.getMaxHealth(), 15, 0.002);
        }
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

    public void draw(MatrixStack stack) {
        if (!this.isEnabled()) {
            return;
        }
        if (e != null) {
            wX = 100;
            re = e;
        } else {
            wX = 0;
        }
        if (re != null) {
            if (!(re instanceof PlayerEntity entity)) {
                return;
            }

            float yOffset = 5;
            double renderWX = renderWX1 / 100d;
            stack.push();
            double rwxI = Math.abs(1 - renderWX);
            AlphaOverride.pushAlphaMul((float) renderWX);
            double x = MathHelper.lerp(rwxI, 0, 0.4) * (modalWidth / 2d);
            double y = MathHelper.lerp(rwxI, 0, 0.4) * (modalHeight / 2d);
            stack.translate(x, y, 0);
            stack.scale((float) MathHelper.lerp(renderWX, 0.6, 1), (float) MathHelper.lerp(renderWX, 0.6, 1), 1);
            double textLeftAlign = 32 + 10;
            Renderer.R2D.renderRoundedQuad(stack, new Color(20, 20, 20, 200), 0, 0, modalWidth, modalHeight, 5, 10);

            Texture tex = PlayerHeadResolver.resolve(entity.getUuid());
            RenderSystem.setShaderTexture(0, tex);

            RenderSystem.enableBlend();
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Renderer.R2D.renderRoundedQuadInternal(stack.peek().getPositionMatrix(), 0, 0, 0, 1, 5, 5, 5 + 32, 5 + 32, 5, 10);

            RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
            Renderer.R2D.renderTexture(stack, 5, 5, 32, 32, 0, 0, 32, 32, 32, 32);
            RenderSystem.defaultBlendFunc();

            FontRenderers.getRenderer().drawString(stack, entity.getEntityName(), textLeftAlign, yOffset, 0xFFFFFF);
            yOffset += FontRenderers.getRenderer().getFontHeight();
            PlayerListEntry ple = Objects.requireNonNull(CoffeeMain.client.getNetworkHandler()).getPlayerListEntry(entity.getUuid());
            if (ple != null && renderPing.getValue()) {
                int ping = ple.getLatency();
                String v = ping + " ms";
                float ww = FontRenderers.getRenderer().getStringWidth(v) + 1;
                FontRenderers.getRenderer().drawString(stack, v, modalWidth - ww - 5, 5, 0xFFFFFF);
            }
            float mhealth = (float) trackedMaxHp;
            float health = (float) trackedHp;
            float hPer = health / mhealth;
            hPer = MathHelper.clamp(hPer, 0, 1);
            double renderToX = MathHelper.lerp(hPer, textLeftAlign, modalWidth - 5);

            Color MID_END = Renderer.Util.lerp(GREEN, RED, hPer);
            double pillHeight = 2;
            Renderer.R2D.renderRoundedQuad(stack, new Color(0, 0, 0, 200), textLeftAlign, modalHeight - 5 - pillHeight, modalWidth - 5, modalHeight - 5, pillHeight / 2d, 10);
            Renderer.R2D.renderRoundedQuad(stack, MID_END, textLeftAlign, modalHeight - 5 - pillHeight, renderToX, modalHeight - 5, pillHeight / 2d, 10);
            if (renderHP.getValue()) {
                FontRenderers.getRenderer().drawString(stack, Utils.Math.roundToDecimal(trackedHp, 2) + " HP", textLeftAlign, yOffset, MID_END.getRGB());
            }

            stack.pop();
            AlphaOverride.popAlphaMul();
        }
    }

}
