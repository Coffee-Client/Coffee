/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper;

import baritone.api.BaritoneAPI;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import coffee.client.CoffeeMain;
import coffee.client.mixinUtil.ChatHudDuck;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BaritoneHelper {
    static List<String> idsWhoWantPause = new CopyOnWriteArrayList<>();
    static int lastMessageId = -1;

    static {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().registerProcess(new IBaritoneProcess() {
            @Override
            public boolean isActive() {
                return isPaused();
            }

            @Override
            public PathingCommand onTick(boolean b, boolean b1) {
                return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
            }

            @Override
            public boolean isTemporary() {
                return true;
            }

            @Override
            public void onLostControl() {

            }

            @Override
            public String displayName0() {
                return "Coffee's Pause / Resume Implementation";
            }

            @Override
            public double priority() {
                return 0d;
            }
        });
    }

    public static void pause(String id) {
        if (idsWhoWantPause.isEmpty()) {
            ((ChatHudDuck) CoffeeMain.client.inGameHud.getChatHud()).coffee_removeChatMessage(lastMessageId);
            lastMessageId = ((ChatHudDuck) CoffeeMain.client.inGameHud.getChatHud()).coffee_addChatMessage(Text.literal("Pausing baritone...")
                                                                                                               .styled(style -> style.withColor(TextColor.fromRgb(Color.YELLOW.getRGB()))));
        }
        if (!idsWhoWantPause.contains(id)) {
            idsWhoWantPause.add(id);
        }
    }

    public static boolean isPaused() {
        return !idsWhoWantPause.isEmpty();
    }

    public static void resume(String id) {
        idsWhoWantPause.remove(id);
        if (idsWhoWantPause.isEmpty()) {
            ((ChatHudDuck) CoffeeMain.client.inGameHud.getChatHud()).coffee_removeChatMessage(lastMessageId);
            lastMessageId = ((ChatHudDuck) CoffeeMain.client.inGameHud.getChatHud()).coffee_addChatMessage(Text.literal("Resuming baritone...")
                                                                                                               .styled(style -> style.withColor(TextColor.fromRgb(new Color(65, 217, 101).getRGB()))));
        }
    }
}
