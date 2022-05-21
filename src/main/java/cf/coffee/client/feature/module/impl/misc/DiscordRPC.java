/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.misc;

import cf.coffee.client.feature.config.StringSetting;
import cf.coffee.client.feature.gui.notifications.Notification;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.util.Utils;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.IPCUser;
import meteordevelopment.discordipc.RichPresence;
import net.minecraft.client.util.math.MatrixStack;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: 05.05.22 change icon
public class DiscordRPC extends Module {
    static final ExecutorService offThreadExec = Executors.newFixedThreadPool(1);
    long updateRequested = 0;
    boolean updateOutstanding = false;
    final StringSetting details = this.config.create(new StringSetting.Builder("Using Coffee").name("Title")
            .description("What to put as the title of the rpc")
            .onChanged(s -> update())
            .get());
    final StringSetting state = this.config.create(new StringSetting.Builder("Obliterating minecraft").name("Description")
            .description("What to put as the description of the rpc")
            .onChanged(s -> update())
            .get());
    long startTime;

    public DiscordRPC() {
        super("DiscordRPC", "Shows a discord rich presence", ModuleType.MISC);

    }

    void update() {
        updateRequested = System.currentTimeMillis() + 2000;
        updateOutstanding = true;
    }

    void actuallyUpdate() {
        if (updateOutstanding) {
            if (updateRequested < System.currentTimeMillis()) {
                updateOutstanding = false;
                setState();
            }
        }
    }

    @Override
    public void onFastTick() {
        actuallyUpdate();
    }

    @Override
    public void tick() {

    }

    void setState() {
        RichPresence rp = new RichPresence();
        rp.setDetails(details.getValue());
        rp.setState(state.getValue());
        rp.setLargeImage("icon", "discord.gg/moles");
        rp.setSmallImage("icon", "0x150#8918");
        rp.setStart(startTime);
        DiscordIPC.setActivity(rp);
    }

    void applyRpc() {
        IPCUser user = DiscordIPC.getUser();
        Utils.Logging.success("Connected to " + user.username + "#" + user.discriminator);
        setState();
        Notification.create(3000, "Discord RPC", Notification.Type.SUCCESS, "Connected!");
    }

    @Override
    public void enable() {
        startTime = Instant.now().getEpochSecond();
        Notification.create(3000, "Discord RPC", Notification.Type.INFO, "Attempting to connect...");
        offThreadExec.execute(() -> {
            boolean result = DiscordIPC.start(971849283911434271L, this::applyRpc);
            if (!result) {
                Notification.create(5000, "Discord RPC", Notification.Type.ERROR, "Discord isn't open! Open discord and enable the module again.");
                setEnabled(false);
            }
        });
    }

    @Override
    public void disable() {
        DiscordIPC.stop();
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
