/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.world;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.MouseEvent;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public class AirPlace extends Module {

    boolean enabled = false;

    public AirPlace() {
        super("AirPlace", "Places blocks in the air", ModuleType.MISC);
        Events.registerEventHandler(EventType.MOUSE_EVENT, event -> {
            if (!this.isEnabled()) return;
            if (enabled && ((MouseEvent) event).getButton() == 1 && ((MouseEvent) event).getAction() == 1) {
                if (CoffeeMain.client.currentScreen != null) return;
                try {
                    if (!client.world.getBlockState(((BlockHitResult) CoffeeMain.client.crosshairTarget).getBlockPos()).isAir())
                        return;
                    CoffeeMain.client.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, (BlockHitResult) CoffeeMain.client.crosshairTarget));
                    if ((client.player.getMainHandStack().getItem() instanceof BlockItem))
                        Renderer.R3D.renderFadingBlock(Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 255), Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 100).darker(), Vec3d.of(((BlockHitResult) CoffeeMain.client.crosshairTarget).getBlockPos()), new Vec3d(1, 1, 1), 1000);
                    CoffeeMain.client.player.swingHand(Hand.MAIN_HAND);
                    event.setCancelled(true);
                } catch (Exception ignored) {
                }
            }
        });
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {
        enabled = true;
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
