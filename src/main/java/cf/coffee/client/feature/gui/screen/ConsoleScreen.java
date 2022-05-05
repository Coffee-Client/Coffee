/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.screen;

import cf.coffee.client.feature.command.Command;
import cf.coffee.client.feature.command.CommandRegistry;
import cf.coffee.client.feature.gui.FastTickable;
import cf.coffee.client.feature.gui.widget.RoundButton;
import cf.coffee.client.feature.gui.widget.RoundTextFieldWidget;
import cf.coffee.client.helper.font.FontRenderers;
import cf.coffee.client.helper.render.ClipStack;
import cf.coffee.client.helper.render.MSAAFramebuffer;
import cf.coffee.client.helper.render.Rectangle;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Transitions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConsoleScreen extends ClientScreen implements FastTickable {
    static final Color BACKGROUND = new Color(60, 60, 60);
    private static ConsoleScreen instance;
    final Color background = new Color(0, 0, 0, 120);
    final List<LogEntry> logs = new ArrayList<>();
    final ClientScreen parent = null;
    RoundTextFieldWidget command;
    double scroll = 0;
    double smoothScroll = 0;
    double lastLogsHeight = 0;

    private ConsoleScreen() {
        super(MSAAFramebuffer.MAX_SAMPLES);
    }

    public static ConsoleScreen instance() {
        if (instance == null) {
            instance = new ConsoleScreen();
        }
        return instance;
    }

    double padding() {
        return 5;
    }

    @Override
    protected void init() {
        super.init();
        double widgetWidthA = width - padding() * 2;
        double buttonWidth = 60;
        double inputWidth = widgetWidthA - buttonWidth - padding();
        command = this.addDrawableChild(new RoundTextFieldWidget(padding(), height - padding() - 20, inputWidth, 20, "Command"));
        RoundButton submit = new RoundButton(new Color(40, 40, 40), padding() * 2 + inputWidth, height - padding() - 20, buttonWidth, 20, "Execute", this::execute);
        addDrawableChild(submit);
        setInitialFocus(command);
    }

    void execute() {
        String cmd = command.get();
        command.setText("");
        if (cmd.isEmpty()) {
            return;
        }
        addLog(new LogEntry("> " + cmd, BACKGROUND));
        CommandRegistry.execute(cmd);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            execute();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_TAB) {
            autocomplete();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        if (parent != null) {
            parent.resize(client, width, height);
        }
        super.resize(client, width, height);
    }

    void autocomplete() {
        String cmd = command.get();
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
        command.setText(String.join(" ", cmdSplit) + " ");
    }

    List<String> getSuggestions(String command) {
        List<String> a = new ArrayList<>();
        String[] args = command.split(" +");
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
        return finalArgs.length > 0 ? a.stream().filter(s -> s.toLowerCase().startsWith(finalArgs[finalArgs.length - 1].toLowerCase())).collect(Collectors.toList()) : a;
    }

    void renderSuggestions(MatrixStack stack) {
        String cmd = command.get();
        float cmdTWidth = FontRenderers.getRenderer().getStringWidth(cmd);
        double cmdXS = command.getX() + 5 + cmdTWidth;
        if (cmd.isEmpty()) {
            return;
        }
        List<String> suggestions = getSuggestions(cmd);
        if (suggestions.isEmpty()) {
            return;
        }
        double probableHeight = suggestions.size() * FontRenderers.getRenderer().getMarginHeight() + padding();
        float yC = (float) (height - padding() - 20 - padding() - probableHeight) - 5;
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

    public void addLog(LogEntry le) {
        logs.add(le);
        if (scroll != 0) {
            scroll += FontRenderers.getRenderer().getMarginHeight(); // keep up when not at 0
            smoothScroll += FontRenderers.getRenderer().getMarginHeight();
        }
    }

    @Override
    public void onFastTick() {
        smoothScroll = Transitions.transition(smoothScroll, scroll, 7, 0);
    }

    @SuppressWarnings("AssignmentToForLoopParameter")
    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {

        command.setFocused(true);
        if (parent != null) {
            parent.renderInternal(stack, mouseX, mouseY, delta);
        }
        Renderer.R2D.renderQuad(stack, background, 0, 0, width, height);

        // log field
        Renderer.R2D.renderRoundedQuad(stack, new Color(20, 20, 20), padding(), padding(), width - padding(), height - padding() - 20 - padding(), 5, 20);
        ClipStack.globalInstance.addWindow(stack, new Rectangle(padding(), padding(), width - padding(), height - padding() - 20 - padding()));
        //Renderer.R2D.beginScissor(stack, padding(), padding(), width - padding(), height - padding() - 20 - padding());

        // logs
        float startingY = (float) (padding() + 5);
        float startingX = (float) (padding() + 5);
        double availWidth = width - padding() - 10;
        double availHeight = height - padding() - 20 - padding() - 13.5;

        List<LogEntryIntern> processedLogs = new ArrayList<>();
        while (logs.size() > 1000) {
            logs.remove(0); // max log size of 1000 before we clear
        }
        for (LogEntry log : logs) {
            List<String> logSplitToWidth = new ArrayList<>();
            StringBuilder currentLog = new StringBuilder();
            char[] logChr = log.text.toCharArray();
            for (int i = 0; i < logChr.length; i++) {
                char current = logChr[i];
                currentLog.append(current);
                if (FontRenderers.getRenderer().getStringWidth(currentLog.toString()) > availWidth) {
                    currentLog.delete(currentLog.length() - 2, currentLog.length());
                    while (currentLog.charAt(currentLog.length() - 1) == ' ') {
                        currentLog.deleteCharAt(currentLog.length() - 1); // clear trailing whitespaces
                    }
                    currentLog.append("-");
                    logSplitToWidth.add(currentLog.toString());
                    currentLog = new StringBuilder();
                    i -= 2;
                }
            }
            logSplitToWidth.add(currentLog.toString());
            processedLogs.add(new LogEntryIntern(logSplitToWidth.toArray(String[]::new), log.color));
        }
        double logsHeight = processedLogs.stream().map(logEntryIntern -> logEntryIntern.text.length * FontRenderers.getRenderer().getMarginHeight()).reduce(Float::sum).orElse(0f);
        lastLogsHeight = logsHeight;
        if (logsHeight > availHeight) {
            startingY -= (logsHeight - availHeight); // scroll up to fit
        }
        startingY += smoothScroll;
        for (LogEntryIntern processedLog : processedLogs) {
            for (String s : processedLog.text) {
                // we're in bounds, render
                if (startingY + FontRenderers.getRenderer().getMarginHeight() >= padding() && startingY <= height - padding() - 20 - padding()) {
                    FontRenderers.getRenderer().drawString(stack, s, startingX, startingY, processedLog.color.getRGB(), false);
                }
                // else, just add
                startingY += FontRenderers.getRenderer().getMarginHeight();
            }
        }

        ClipStack.globalInstance.popWindow();
        //Renderer.R2D.endScissor();

        if (logsHeight > availHeight) {
            double viewportHeight = (height - padding() - 20 - padding()) - padding();
            double contentHeight = processedLogs.stream().map(logEntryIntern -> logEntryIntern.text.length * FontRenderers.getRenderer().getMarginHeight()).reduce(Float::sum).orElse(0f);
            double per = viewportHeight / contentHeight;
            double barHeight = (height - padding() - 20 - padding() * 2) - padding() * 2;
            double innerbarHeight = barHeight * per;
            double perScrolledIndex = smoothScroll / Math.max(1, lastLogsHeight - (height - padding() - 20 - padding() - 13.5));
            perScrolledIndex = 1 - perScrolledIndex;
            double wid = 3;
            double cursorY = MathHelper.lerp(perScrolledIndex, padding() * 2, height - padding() - 20 - padding() * 2 - innerbarHeight);
            double cursorX = width - padding() * 2 - 3;

            Renderer.R2D.renderRoundedQuad(stack, new Color(10, 10, 10, 150), cursorX - wid / 2d, padding() * 2, cursorX + wid / 2d, padding() * 2 + barHeight, wid / 2d, 10);

            //            Renderer.R2D.renderCircle(stack, new Color(50, 50, 50, 150), cursorX, cursorY, 3, 10);
            Renderer.R2D.renderRoundedQuad(stack, new Color(50, 50, 50, 150), cursorX - wid / 2d, cursorY, cursorX + wid / 2d, cursorY + per * barHeight, wid / 2d, 10);
        }

        renderSuggestions(stack);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scroll += (amount * 10);
        scroll = MathHelper.clamp(scroll, 0, Math.max(0, lastLogsHeight - (height - padding() - 20 - padding() - 13.5)));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void close() {
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public record LogEntry(String text, Color color) {

    }

    protected record LogEntryIntern(String[] text, Color color) {
    }
}
