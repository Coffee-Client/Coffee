/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.misc.MoreChatHistory;
import coffee.client.mixinUtil.ChatHudDuck;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import oshi.util.tuples.Pair;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin implements ChatHudDuck {


    private final Int2ObjectMap<Pair<ChatHudLine, ChatHudLine.Visible>> idToHudLineMap = new Int2ObjectArrayMap<>();
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int scrolledLines;
    @Shadow
    private boolean hasUnreadNewMessages;
    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    @Final
    private List<ChatHudLine> messages;

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract double getChatScale();

    @Shadow
    protected abstract boolean isChatFocused();

    @Shadow
    public abstract void scroll(int scroll);

    @Override
    public int coffee_addChatMessage(Text content) {
        CoffeeMain.log(Level.INFO, "[Client chat]", content.getString());
        int i = MathHelper.floor((double) this.getWidth() / this.getChatScale());

        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(content, i, this.client.textRenderer);
        boolean bl = this.isChatFocused();
        int ticks = this.client.inGameHud.getTicks();
        ChatHudLine.Visible element = null;
        for (int j = 0; j < list.size(); ++j) {
            OrderedText orderedText = list.get(j);
            if (bl && this.scrolledLines > 0) {
                this.hasUnreadNewMessages = true;
                this.scroll(1);
            }

            boolean bl2 = j == list.size() - 1;
            element = new ChatHudLine.Visible(ticks, orderedText, MessageIndicator.system(), bl2);
            this.visibleMessages.add(0, element);
        }


        MoreChatHistory byClass = ModuleRegistry.getByClass(MoreChatHistory.class);
        int history = byClass.isEnabled() ? byClass.getHistSize() : 100;
        while (this.visibleMessages.size() > history) {
            ChatHudLine.Visible visible = this.visibleMessages.get(this.visibleMessages.size() - 1);
            for (int integer : new IntArraySet(this.idToHudLineMap.keySet())) {
                Pair<ChatHudLine, ChatHudLine.Visible> chatHudLineVisiblePair = this.idToHudLineMap.get(integer);
                if (visible.equals(chatHudLineVisiblePair.getB())) {
                    this.idToHudLineMap.remove(integer);
                }
            }
            this.visibleMessages.remove(visible);
        }

        ChatHudLine element1 = new ChatHudLine(ticks, content, null, MessageIndicator.system());
        this.messages.add(0, element1);

        int indexFound = -1;
        for (int i1 = 0; i1 <= this.idToHudLineMap.size(); i1++) {
            if (!this.idToHudLineMap.containsKey(i1)) {
                indexFound = i1;
                break;
            }
        }
        //        if (indexFound == -1) indexFound = this.idToHudLineMap.size();
        this.idToHudLineMap.put(indexFound, new Pair<>(element1, element));

        while (this.messages.size() > history) {
            this.messages.remove(this.messages.size() - 1);
        }
        return indexFound;
    }

    @Override
    public void coffee_removeChatMessage(int id) {
        Pair<ChatHudLine, ChatHudLine.Visible> chatHudLineVisiblePair = this.idToHudLineMap.get(id);
        if (chatHudLineVisiblePair == null) {
            return;
        }
        this.visibleMessages.remove(chatHudLineVisiblePair.getB());
        this.messages.remove(chatHudLineVisiblePair.getA());
        this.idToHudLineMap.remove(id);
    }
}
