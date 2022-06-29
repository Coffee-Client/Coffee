/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.StringSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.MouseEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class Annihilator extends Module {

    final DoubleSetting range = this.config.create(new DoubleSetting.Builder(5).name("Range")
            .description("Range of the nuke")
            .min(1)
            .max(14)
            .precision(0)
            .get());
    final StringSetting block = this.config.create(new StringSetting.Builder("air").name("Block").description("The block to fill with").get());


    public Annihilator() {
        super("Annihilator", "Nukes whatever you click at, requires /fill permissions", ModuleType.WORLD);
        Events.registerEventHandler(EventType.MOUSE_EVENT, event -> {
            if (!this.isEnabled()) {
                return;
            }
            if (client.player == null) {
                return;
            }
            MouseEvent event1 = (MouseEvent) event;
            if (event1.getButton() == 0 && event1.getAction() == 1) {
                mousePressed();
            }
        });
    }

    void mousePressed() {
        if (client.currentScreen != null) {
            return;
        }
        HitResult hr = Objects.requireNonNull(client.player).raycast(200d, 0f, true);
        Vec3d pos1 = hr.getPos();
        BlockPos pos = new BlockPos(pos1);
        int startY = MathHelper.clamp(r(pos.getY() - range.getValue()),
                Objects.requireNonNull(CoffeeMain.client.world).getBottomY(),
                CoffeeMain.client.world.getTopY()
        );
        int endY = MathHelper.clamp(r(pos.getY() + range.getValue()), CoffeeMain.client.world.getBottomY(), CoffeeMain.client.world.getTopY());
        String cmd = "/fill " + r(pos.getX() - range.getValue()) + " " + startY + " " + r(pos.getZ() - range.getValue()) + " " + r(pos.getX() + range.getValue()) + " " + endY + " " + r(
                pos.getZ() + range.getValue()) + " " + "minecraft:" + block.getValue();
        //        System.out.println(cmd);
        client.player.sendChatMessage(cmd);
    }

    int r(double v) {
        return (int) Math.round(v);
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
