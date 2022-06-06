/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
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
import coffee.client.helper.render.Renderer;
import lombok.val;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Debug;
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

@Debug(export = true)
@Mixin(ChatScreen.class)
public class AChatScreenMixin extends Screen {

    @Shadow
    protected TextFieldWidget chatField;
    String previousSuggestionInput = "";
    @Shadow
    CommandSuggestor commandSuggestor;

    protected AChatScreenMixin(Text title) {
        super(title);
    }

    private String getPrefix() {
        return ModuleRegistry.getByClass(ClientSettings.class).getPrefix().getValue();
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;)V"))
    void coffee_interceptChatMessage(ChatScreen instance, String s) {
        String p = getPrefix();
        if (SelfDestruct.shouldSelfDestruct()) {
            if (SelfDestruct.handleMessage(s)) {
                return;
            }
        } else if (s.startsWith(p)) { // filter all messages starting with .
            CoffeeMain.client.inGameHud.getChatHud().addToMessageHistory(s);
            CommandRegistry.execute(s.substring(p.length())); // cut off prefix
            return;
        }
        instance.sendMessage(s); // else, go
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
                a = List.of(c.getSuggestionsWithType(args.length - 1, args).suggestions());
            } else {
                return new ArrayList<>(); // we have no command to ask -> we have no suggestions
            }
        } else {
            for (Command command1 : CommandRegistry.getCommands()) {
                for (String alias : command1.getAliases()) {
                    if (alias.toLowerCase().startsWith(cmd.toLowerCase())) {
                        a.add(alias);
                    }
                }
            }
        }
        String[] finalArgs = args;
        return finalArgs.length > 0 ? a.stream()
                .filter(s -> s.toLowerCase().startsWith(finalArgs[finalArgs.length - 1].toLowerCase()))
                .collect(Collectors.toList()) : a;
    }

    double padding() {
        return 5;
    }

    void renderSuggestions(MatrixStack stack) {
        String p = getPrefix();
        String cmd = chatField.getText().substring(p.length());
        if (cmd.isEmpty()) {
            return;
        }
        float cmdTWidth = CoffeeMain.client.textRenderer.getWidth(cmd);
        double cmdXS = chatField.x + 5 + cmdTWidth;

        List<String> suggestions = getSuggestions(cmd);
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
        Renderer.R2D.renderRoundedQuad(stack,
                new Color(30, 30, 30, 255),
                xC - padding(),
                yC - padding(),
                xC + probableWidth + padding(),
                yC + probableHeight,
                5,
                20);
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
        List<String> suggestions = getSuggestions(cmd);
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
            renderSuggestions(matrices);
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
        return ((ICommandSuggestorMixin) this.commandSuggestor).invokeProvideRenderText(s, integer);
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
                            ArgumentType current = c.getSuggestionsWithType(countedGaps - 1, args).type();
                            int col = 0xFFFFFF;
                            if (current != null) {
                                col = current.getColor().getRGB();
                            }
                            texts.add(OrderedText.styledForwardsVisitedString(String.valueOf(c1),
                                    Style.EMPTY.withColor(col)));
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
