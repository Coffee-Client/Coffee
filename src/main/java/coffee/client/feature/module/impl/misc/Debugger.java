/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.Packet;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Debugger extends Module {
    public static List<Class<? extends Packet<?>>> whitelistedPacketClasses = new ArrayList<>();
    public static List<LogEntry> logs = new CopyOnWriteArrayList<>();
    public Debugger() {
        super("Debugger", "the fucking", ModuleType.HIDDEN);
    }

    public static void log(String n) {
        logs.add(new LogEntry(n, System.currentTimeMillis()));
    }

    @EventListener(value = EventType.PACKET_RECEIVE, prio = 999)
    void onIncoming(PacketEvent incoming) {
        logPacket(incoming);
    }

    @EventListener(value = EventType.PACKET_SEND, prio = 999)
    void onOutgoing(PacketEvent o) {
        logPacket(o);
    }

    void logPacket(PacketEvent incoming) {
        Packet<?> packet = incoming.getPacket();
        if (whitelistedPacketClasses.stream().anyMatch(aClass -> aClass.isInstance(packet))) {
            boolean cancelled = incoming.isCancelled();
            String mapped = FabricLoader.getInstance().getMappingResolver().unmapClassName("named", packet.getClass().getName());
            String[] split = mapped.split("\\.");
            mapped = split[split.length - 1];
            log(String.format("Cancelled: %s, %s", cancelled, Utils.forceToString(mapped, packet)));
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
        logs.removeIf(LogEntry::hasExpired);
        while (logs.size() > 40) {
            logs.remove(0);
        }
        FontAdapter fa = FontRenderers.getMonoSize(10);
        double yOff = 0;
        double width = fa.getStringWidth(logs.stream().map(LogEntry::text).max(Comparator.comparingInt(String::length)).orElse(""));
        MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
        for (LogEntry log : logs) {
            Renderer.R2D.renderQuad(ms,
                new Color(0, 0, 0, 200),
                client.getWindow().getScaledWidth() - 1 - width,
                1 + yOff,
                client.getWindow().getScaledWidth() - 1,
                1 + yOff + fa.getFontHeight());
            fa.drawString(ms, log.text, client.getWindow().getScaledWidth() - 1 - width, 1 + yOff, 0xFFFFFF);
            yOff += fa.getFontHeight();
        }
    }

    record LogEntry(String text, long added) {
        public boolean hasExpired() {
            return System.currentTimeMillis() - added > 6000;
        }
    }
}
