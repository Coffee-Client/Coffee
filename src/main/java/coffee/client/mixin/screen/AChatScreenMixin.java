/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.CommandRegistry;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.misc.ClientSettings;
import coffee.client.feature.module.impl.misc.InfChatLength;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import lombok.val;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ChatScreen.class)
public class AChatScreenMixin extends Screen {

    private final List<String> cachedSuggestions = new ArrayList<>();
    @Shadow
    protected TextFieldWidget chatField;
    String previousSuggestionInput = "";
    @Shadow
    ChatInputSuggestor chatInputSuggestor;
    private String previousCommand = "";

    protected AChatScreenMixin(Text title) {
        super(title);
    }

    private String getPrefix() {
        return ModuleRegistry.getByClass(ClientSettings.class).getPrefix().getValue();
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;Z)Z"))
    boolean coffee_interceptChatMessage(ChatScreen instance, String s, boolean t) {
        String p = getPrefix();
        if (SelfDestruct.shouldSelfDestruct()) {
            if (SelfDestruct.handleMessage(s)) {
                return true;
            }
        } else if (s.startsWith(p)) { // filter all messages starting with .
            CoffeeMain.client.inGameHud.getChatHud().addToMessageHistory(s);
            CommandRegistry.execute(s.substring(p.length())); // cut off prefix
            return true;
        }
        instance.sendMessage(s, true); // else, go
        return true;
    }

    double padding() {
        return 5;
    }


    List<String> getSuggestions(String command) {
        List<String> a = new ArrayList<>();
        String[] args = command.split(" +");
        if (args.length == 0) {
            return a;
        }
        String cmd = args[0].toLowerCase();
        args = Arrays.copyOfRange(args, 1, args.length);
        if (command.endsWith(" ")) { // append empty arg when we end with a space
            String[] args1 = new String[args.length + 1];
            System.arraycopy(args, 0, args1, 0, args.length);
            args1[args1.length - 1] = "";
            args = args1;
        }
        if (args.length > 0) {
            Command c = CommandRegistry.getByAlias(cmd);
            if (c != null) {
                a = List.of(c.getSuggestionsWithType(args.length - 1, args).suggestionSupplier().get());
            } else {
                return new ArrayList<>(); // we have no command to ask -> we have no suggestions
            }
        } else {
            outer:
            for (Command command1 : CommandRegistry.getCommands()) {
                for (String alias : command1.getAliases()) {
                    if (a.size() > 20) {
                        break outer;
                    }
                    if (!Utils.searchMatches(alias, cmd).isEmpty()) {
                        a.add(alias);
                    }
                }
            }
        }
        String[] finalArgs = args;
        return args.length > 0 ? a.stream()
            .filter(s -> s.startsWith("<") || !Utils.searchMatches(s, finalArgs[finalArgs.length - 1]).isEmpty())
            .distinct()
            .limit(20)
            .collect(Collectors.toList()) : a;
    }

    void renderSuggestions(MatrixStack stack) {
        String p = getPrefix();
        String cmd = chatField.getText().substring(p.length());
        if (cmd.isEmpty()) {
            return;
        }
        boolean changed = !previousCommand.equals(cmd);
        previousCommand = cmd;
        float cmdTWidth = CoffeeMain.client.textRenderer.getWidth(cmd);
        double cmdXS = chatField.x + 5 + cmdTWidth;

        List<String> suggestions = changed ? Util.make(getSuggestions(cmd), strings -> {
            if (strings.size() >= 20) {
                strings.add("...");
            }
        }) : cachedSuggestions;
        if (changed) {
            cachedSuggestions.clear();
            cachedSuggestions.addAll(suggestions);
        }
        if (suggestions.isEmpty()) {
            return;
        }
        double probableHeight = suggestions.size() * FontRenderers.getRenderer().getMarginHeight() + padding();
        float yC = (float) (chatField.y - padding() - probableHeight);
        double probableWidth = 0;
        for (String suggestion : suggestions) {
            probableWidth = Math.max(probableWidth, FontRenderers.getRenderer().getStringWidth(suggestion) + 1);
        }
        float xC = (float) (cmdXS);
        Renderer.R2D.renderRoundedQuad(stack, new Color(30, 30, 30, 255), xC - padding(), yC - padding(), xC + probableWidth + padding(), yC + probableHeight, 5, 20);
        for (String suggestion : suggestions) {
            FontRenderers.getRenderer().drawString(stack, suggestion, xC, yC, 0xFFFFFF, false);
            yC += FontRenderers.getRenderer().getMarginHeight();
        }
    }

    void autocomplete() {
        String p = getPrefix();
        String cmd = chatField.getText().substring(p.length());
        if (cmd.isEmpty()) {
            return;
        }
        List<String> suggestions = getSuggestions(cmd).stream().map(s -> s.split("<")[0]) // strip everything after the first <
            .filter(s -> !s.isEmpty()) // filter empty suggestions
            .toList();
        if (suggestions.isEmpty()) {
            return;
        }
        String[] cmdSplit = cmd.split(" +");
        if (cmd.endsWith(" ")) {
            String[] cmdSplitNew = new String[cmdSplit.length + 1];
            System.arraycopy(cmdSplit, 0, cmdSplitNew, 0, cmdSplit.length);
            cmdSplitNew[cmdSplitNew.length - 1] = "";
            cmdSplit = cmdSplitNew;
        }
        cmdSplit[cmdSplit.length - 1] = suggestions.get(0);
        chatField.setText(p + String.join(" ", cmdSplit) + " ");
        chatField.setCursorToEnd();
    }

    @Inject(method = "render", at = @At("RETURN"))
    void coffee_renderSuggestions(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String p = getPrefix();
        String t = chatField.getText();
        if (t.startsWith(p) && !SelfDestruct.shouldSelfDestruct()) {
            MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> renderSuggestions(matrices));
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    void coffee_autocomplete(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        String p = getPrefix();
        if (keyCode == GLFW.GLFW_KEY_TAB && chatField.getText().startsWith(p) && !SelfDestruct.shouldSelfDestruct()) {
            autocomplete();
            cir.setReturnValue(true);
        }
    }

    OrderedText vanillaTextProvider(String s, int integer) {
        return ((ICommandSuggestorMixin) this.chatInputSuggestor).invokeProvideRenderText(s, integer);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void coffee_init(CallbackInfo ci) {
        if (SelfDestruct.shouldSelfDestruct()) {
            return;
        }
        chatField.setMaxLength((ModuleRegistry.getByClass(InfChatLength.class).isEnabled()) ? Integer.MAX_VALUE : 256);
        chatField.setRenderTextProvider((s, integer) -> {
            String t;
            if (integer == 0) {
                previousSuggestionInput = s;
                t = s;
            } else {
                t = previousSuggestionInput + s;
            }
            if (t.isEmpty()) {
                return OrderedText.empty();
            }
            String p = getPrefix();
            if (t.length() <= p.length()) {
                return vanillaTextProvider(s, integer);
            }

            String actualCommandText = t.substring(p.length());
            String[] spl = actualCommandText.split(" +");

            if (spl.length == 0) {
                return vanillaTextProvider(s, integer);
            }
            Command c = CommandRegistry.getByAlias(spl[0]);
            String[] args = Arrays.copyOfRange(spl, 1, spl.length);
            if (c != null && t.startsWith(p) && args.length > 0) {
                List<OrderedText> texts = new ArrayList<>();
                int countedGaps = 0;
                boolean countedSpaceBefore = false;
                val chars = t.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    char c1 = chars[i];
                    if (c1 == ' ') {
                        if (!countedSpaceBefore) {
                            countedGaps++;
                        }
                        countedSpaceBefore = true;
                        if (i >= integer) {
                            texts.add(OrderedText.styledForwardsVisitedString(String.valueOf(c1), Style.EMPTY));
                        }
                    } else {
                        countedSpaceBefore = false;
                        if (i < integer) {
                            continue;
                        }
                        if (countedGaps >= 1) {
                            ArgumentType current = c.getSuggestionsWithType(countedGaps - 1, args).argType();
                            int col = 0xFFFFFF;
                            if (current != null) {
                                col = current.getColor().getRGB();
                            }
                            texts.add(OrderedText.styledForwardsVisitedString(String.valueOf(c1), Style.EMPTY.withColor(col)));
                        } else {
                            texts.add(OrderedText.styledForwardsVisitedString(String.valueOf(c1), Style.EMPTY));
                        }
                    }
                }
                return OrderedText.concat(texts);
            }
            return vanillaTextProvider(s, integer);
        });
    }
}
