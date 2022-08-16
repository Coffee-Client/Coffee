/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.util.Utils;
import coffee.client.mixin.network.IParticleS2CPacketMixin;
import coffee.client.mixinUtil.ParticleManagerDuck;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;

public class AntiCrash extends Module {
    private static AntiCrash instance = null;
    final BooleanSetting screenGui = this.config.create(new BooleanSetting.Builder(false).name("Cap Screens").description("Prevents too many screens from being opened").get());
    final BooleanSetting capVel = this.config.create(new BooleanSetting.Builder(true).name("Cap velocity").description("Prevents an abnormally sized velocity packet from going through").get());
    @Getter
    final BooleanSetting capParticles = this.config.create(new BooleanSetting.Builder(true).name("Cap particles").description("Prevents too many particles from being rendered").get());
    @Getter
    final DoubleSetting particleMax = this.config.create(new DoubleSetting.Builder(1000).name("Particle max").description("How many particles to allow at once").min(0).max(50000).precision(0).get());
    @Getter
    final BooleanSetting capNames = this.config.create(new BooleanSetting.Builder(true).name("Cap entity names").description("Cap the max size an entity name can be").get());
    @Getter
    final DoubleSetting nameMax = this.config.create(new DoubleSetting.Builder(64).name("Name max").description("How long a name should be allowed to be").min(6).max(100).precision(0).get());

    @Getter
    final BooleanSetting disableBossbars = this.config.create(new BooleanSetting.Builder(true).name("Disable bossbars").description("Does not render bossbars").get());

    @Getter
    final BooleanSetting disableObfText = this.config.create(new BooleanSetting.Builder(true).name("Disable obfuscation").description("Disables obfuscated text").get());

    @Getter
    final BooleanSetting capAttributes = this.config.create(new BooleanSetting.Builder(true).name("Cap attributes").description("Caps the amount of attributes being rendered").get());

    @Getter
    final DoubleSetting capAttributesAmount = this.config.create(
            new DoubleSetting.Builder(4).name("Cap attrib. max").description("How many attributes should be allowed to render (per modifier equipment slot)").min(0).max(64).precision(0).get());


    long lastScreen = System.currentTimeMillis();
    Notification lastCrashNotif = null;

    public AntiCrash() {
        super("AntiCrash", "Prevents you from being fucked", ModuleType.MISC);
        nameMax.showIf(capNames::getValue);
        particleMax.showIf(capParticles::getValue);
        capAttributesAmount.showIf(capAttributes::getValue);
    }

    public static AntiCrash instance() {
        if (instance == null) {
            instance = ModuleRegistry.getByClass(AntiCrash.class);
        }
        return instance;
    }

    @EventListener(EventType.PACKET_RECEIVE)
    void handlePacketEvent(PacketEvent e) {
        if (!this.isEnabled()) {
            return;
        }
        if (e.getPacket() instanceof OpenScreenS2CPacket && screenGui.getValue()) {
            long current = System.currentTimeMillis();
            long diff = current - lastScreen;
            lastScreen = current;
            if (diff < 10) {
                showCrashPreventionNotification("Server sent open screen packet too fast!");
                e.setCancelled(true);
            }
        }
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket p && capVel.getValue()) {
            double vx = p.getVelocityX() / 8000d;
            double vy = p.getVelocityY() / 8000d;
            double vz = p.getVelocityZ() / 8000d;
            if (Math.abs(vx) >= 20 || Math.abs(vy) >= 20 || Math.abs(vz) >= 20) {
                Utils.Logging.warn(String.format("Server sent abnormally big velocity packet (X:%f Y:%f Z:%f)", vx, vy, vz));
            }
        }
        if (e.getPacket() instanceof ParticleS2CPacket p && capParticles.getValue()) {
            int partTotal = ((ParticleManagerDuck) CoffeeMain.client.particleManager).getTotalParticles();
            int newCount = partTotal + p.getCount();
            if (newCount >= particleMax.getValue()) {
                int space = (int) Math.floor(particleMax.getValue() - partTotal);
                if (space > 0) {
                    ((IParticleS2CPacketMixin) p).setCount(Math.min(space, p.getCount())); // decrease count to fit just below particle max
                    //                    showCrashPreventionNotification("Decreased particle packet: " + oldCount + " -> " + p.getCount());
                } else {
                    //                    showCrashPreventionNotification("Blocked particle packet: S=" + p.getCount() + " T=" + partTotal);
                    e.setCancelled(true);
                }
            }
        }
    }

    public void showCrashPreventionNotification(String msg) {
        if (lastCrashNotif == null || lastCrashNotif.creationDate + lastCrashNotif.duration < System.currentTimeMillis()) {
            lastCrashNotif = Notification.create(4000, "AntiCrash", Notification.Type.WARNING, msg);
        }
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
