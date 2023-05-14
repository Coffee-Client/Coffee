/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.Keybind;
import coffee.client.helper.util.Rotations;
import lombok.Getter;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class FreeLook extends Module {
    private static FreeLook instance = null;
    final BooleanSetting hold = this.config.create(new BooleanSetting.Builder(true).name("Hold").description("Disables the module after you unpress the keybind").get());
    @Getter
    final BooleanSetting enableAA = this.config.create(new BooleanSetting.Builder(false).name("Enable Anti-Aim").description("Hvh toggle rage nn noob").get());
    final EnumSetting<AntiAimMode> aaMode = this.config.create(new EnumSetting.Builder<>(AntiAimMode.Spin).name("AA Mode").description("How to aim").get());
    final DoubleSetting aaSpeed = this.config.create(new DoubleSetting.Builder(1).name("AA Speed").description("How fast to aim").min(0.1).max(6).precision(1).get());
    final DoubleSetting jitterRange = this.config.create(new DoubleSetting.Builder(90).name("Jitter range")
                                                                                      .description("How far to jitter")
                                                                                      .min(15)
                                                                                      .max(90)
                                                                                      .precision(0)
                                                                                      .get());
    final DoubleSetting swayRange = this.config.create(new DoubleSetting.Builder(45).name("Sway range")
                                                                                    .description("How far to sway")
                                                                                    .min(15)
                                                                                    .max(60)
                                                                                    .precision(0)
                                                                                    .get());
    public float newyaw, newpitch, oldyaw, oldpitch;
    Perspective before = Perspective.FIRST_PERSON;
    Keybind kb;
    int jittertimer = 0;
    int swayYaw = 0;

    public FreeLook() {
        super("FreeLook", "The lunar freelook but without the restrictions", ModuleType.RENDER);
        aaMode.showIf(enableAA::getValue);
        aaSpeed.showIf(() -> aaMode.getValue() != AntiAimMode.Jitter && enableAA.getValue());
        jitterRange.showIf(() -> aaMode.getValue() == AntiAimMode.Jitter && enableAA.getValue());
        swayRange.showIf(() -> aaMode.getValue() == AntiAimMode.Sway && enableAA.getValue());
    }

    public static FreeLook instance() {
        if (instance == null) {
            instance = ModuleRegistry.getByClass(FreeLook.class);
        }
        return instance;
    }

    @Override
    public void tick() {
        if (kb == null) {
            return;
        }
        if (!kb.isPressed() && hold.getValue()) {
            this.setEnabled(false);
        }

        Rotations.setClientPitch(newpitch);
        Rotations.setClientYaw(newyaw);
    }

    @Override
    public void enable() {
        kb = new Keybind((int) (keybind.getValue() + 0));
        before = client.options.getPerspective();
        oldyaw = Objects.requireNonNull(client.player).getYaw();
        oldpitch = client.player.getPitch();
        newyaw = client.player.getYaw();
        if (enableAA.getValue()) {
            newpitch = 90;
        } else {
            newpitch = client.player.getPitch();
        }
    }

    @Override
    public void disable() {
        client.options.setPerspective(before);
        Objects.requireNonNull(client.player).setYaw(oldyaw);
        client.player.setPitch(oldpitch);
    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void onFastTick() {
        if (!enableAA.getValue()) {
            return;
        }
        switch (aaMode.getValue()) {
            case Spin -> newyaw = (float) MathHelper.wrapDegrees(newyaw + aaSpeed.getValue());
            case Jitter -> {
                int temp = (int) (jitterRange.getValue() + 0);
                if (jittertimer == 1) {
                    temp *= -1;
                }
                if (jittertimer >= 1) {
                    jittertimer = -1;
                }
                jittertimer++;
                newyaw = MathHelper.wrapDegrees(Objects.requireNonNull(client.player).getYaw() + 180 + temp);
            }
            case Sway -> {
                int temp = swayYaw;
                if (temp >= swayRange.getValue() * 2) {
                    temp = (int) (swayRange.getValue() + 0) - (swayYaw - (int) (swayRange.getValue() * 2));
                } else {
                    temp = (int) (swayRange.getValue() * -1) + swayYaw;
                }
                if (swayYaw >= swayRange.getValue() * 4) {
                    swayYaw = 0;
                }
                swayYaw += aaSpeed.getValue();
                newyaw = MathHelper.wrapDegrees(Objects.requireNonNull(client.player).getYaw() + 180 + temp);
            }
        }
        Objects.requireNonNull(client.getNetworkHandler())
               .sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(newyaw, newpitch, Objects.requireNonNull(client.player).isOnGround()));
    }

    public enum AntiAimMode {
        Spin, Jitter, Sway
    }
}
