/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.Profiler;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
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
            log(String.format("Cancelled: %s, %s", cancelled, Utils.forceToString(packet)));
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
        List<Profiler.ProfilerEntry> entries = Profiler.getEntries();
        int count = entries.stream().map(profilerEntry -> profilerEntry.getSubEntries().size() + 1).reduce(Integer::sum).orElse(0);
        width = 0;
        for (Profiler.ProfilerEntry entry : entries) {
            String t = String.format("%s: %d (%.2f ms)", entry.getName(), entry.getDuration(), entry.getDuration() / 1e6);
            width = Math.max(width, fa.getStringWidth(t));
            for (Profiler.ProfilerEntry subEntry : entry.getSubEntries()) {
                t = String.format(" -> %s: %d (%.2f ms)", subEntry.getName(), subEntry.getDuration(), subEntry.getDuration() / 1000d);
                width = Math.max(width, fa.getStringWidth(t));
            }
        }
        yOff = fa.getFontHeight() * count;
        for (Profiler.ProfilerEntry entry : entries) {
            String t = String.format("%s: %d (%.2f ms)", entry.getName(), entry.getDuration(), entry.getDuration() / 1e6);
            Renderer.R2D.renderQuad(ms,
                Color.BLACK,
                client.getWindow().getScaledWidth() - 1 - width,
                client.getWindow().getScaledHeight() - 1 - yOff,
                client.getWindow().getScaledWidth() - 1,
                client.getWindow().getScaledHeight() - 1);
            fa.drawString(ms, t, client.getWindow().getScaledWidth() - 1 - width, client.getWindow().getScaledHeight() - 1 - yOff, 0xFFFFFF);
            yOff -= fa.getFontHeight();
            for (Profiler.ProfilerEntry subEntry : entry.getSubEntries()) {
                t = String.format(" -> %s: %d (%.2f ms)", subEntry.getName(), subEntry.getDuration(), entry.getDuration() / 1e6);
                fa.drawString(ms, t, client.getWindow().getScaledWidth() - 1 - width, client.getWindow().getScaledHeight() - 1 - yOff, 0xFFFFFF);
                yOff -= fa.getFontHeight();
            }
        }
    }

    record LogEntry(String text, long added) {
        public boolean hasExpired() {
            return System.currentTimeMillis() - added > 6000;
        }
    }
}
