/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.widget.RoundButton;
import coffee.client.feature.gui.widget.RoundTextFieldWidget;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ProxyManagerScreen extends ClientScreen {
    static final double widgetWidth = 300;
    static final FontAdapter title = FontRenderers.getCustomSize(40);
    public static Proxy currentProxy = null;
    static double widgetHeight = 400;
    static boolean isSocks4 = false;
    final Screen parent;
    RoundTextFieldWidget ip, port, user, pass;
    RoundButton reset, apply, type;

    public ProxyManagerScreen(Screen parent) {
        super(MSAAFramebuffer.MAX_SAMPLES);
        this.parent = parent;
    }

    double padding() {
        return 5;
    }

    @Override
    protected void init() {

        RoundButton exit = new RoundButton(new Color(40, 40, 40), width - 20 - 5, 5, 20, 20, "X", this::close);
        addDrawableChild(exit);

        double wWidth = widgetWidth - padding() * 2d;
        double sourceX = width / 2d - widgetWidth / 2d + wWidth / 2d;
        double sourceY = height / 2d - widgetHeight / 2d;
        double yOffset = padding() + title.getMarginHeight() + FontRenderers.getRenderer()
                .getMarginHeight() + padding();
        ip = new RoundTextFieldWidget(sourceX, sourceY + yOffset, wWidth, 20, "IP");
        yOffset += ip.getHeight() + padding();
        port = new RoundTextFieldWidget(sourceX, sourceY + yOffset, wWidth, 20, "Port");

        yOffset += port.getHeight() + padding();
        user = new RoundTextFieldWidget(sourceX, sourceY + yOffset, wWidth, 20, "Username (opt.)");
        yOffset += user.getHeight() + padding();
        pass = new RoundTextFieldWidget(sourceX, sourceY + yOffset, wWidth, 20, "Password (opt.)");
        yOffset += pass.getHeight() + padding();
        if (currentProxy != null) {
            ip.setText(currentProxy.address);
            port.setText(currentProxy.port + "");
            user.setText(currentProxy.user);
            pass.setText(currentProxy.pass);
        }
        type = new RoundButton(new Color(40, 40, 40), sourceX, sourceY + yOffset, wWidth, 20, "Type: " + (isSocks4 ? "Socks4" : "Socks5"), () -> {
            isSocks4 = !isSocks4;
            type.setText("Type: " + (isSocks4 ? "Socks4" : "Socks5"));
        });

        yOffset += 20 + padding();
        double doubleWidth = wWidth / 2d - padding() / 2d;
        reset = new RoundButton(new Color(40, 40, 40), sourceX, yOffset, doubleWidth, 20, "Reset", () -> {
            currentProxy = null;
            ip.set("");
            port.set("");
            user.set("");
            pass.set("");
        });
        apply = new RoundButton(new Color(40, 40, 40), sourceX + doubleWidth + padding(), yOffset, doubleWidth, 20, "Apply", () -> currentProxy = new Proxy(ip.get(), Integer.parseInt(port.get()), isSocks4, user.get(), pass.get()));

        addDrawableChild(ip);
        addDrawableChild(port);
        addDrawableChild(user);
        addDrawableChild(pass);
        addDrawableChild(type);
        addDrawableChild(reset);
        addDrawableChild(apply);


        super.init();
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        if (parent != null) {
            parent.render(stack, mouseX, mouseY, delta);
        }
        Renderer.R2D.renderQuad(stack, new Color(0, 0, 0, 130), 0, 0, width, height);
        double wWidth = widgetWidth - padding() * 2d;
        double sourceX = width / 2d - widgetWidth / 2d + padding();
        double sourceY = height / 2d - widgetHeight / 2d;
        double actualSourceX = width / 2d - widgetWidth / 2d;
        double yOffset = 1;
        Renderer.R2D.renderRoundedQuad(stack, new Color(20, 20, 20), width / 2d - widgetWidth / 2d, height / 2d - widgetHeight / 2d, width / 2d + widgetWidth / 2d, height / 2d + widgetHeight / 2d, 5, 20);
        title.drawString(stack, "Proxies", (float) (actualSourceX + padding()), (float) (sourceY + yOffset), 0xFFFFFF, false);
        yOffset += title.getMarginHeight();
        String t = "Manage your proxy connection";
        FontRenderers.getRenderer()
                .drawString(stack, t, (float) (actualSourceX + padding()), (float) (sourceY + yOffset), 0xFFFFFF, false);
        if (currentProxy != null) {
            String text = "Connected: " + currentProxy.address + ":" + currentProxy.port;
            double textWidth = FontRenderers.getRenderer().getStringWidth(text);
            FontRenderers.getRenderer()
                    .drawString(stack, text, (float) (actualSourceX + widgetWidth - padding() - textWidth), (float) (sourceY + yOffset), 0xFFFFFF, false);
        }
        yOffset += FontRenderers.getRenderer().getMarginHeight() + padding();

        ip.setX(sourceX);
        ip.setY(sourceY + yOffset);
        yOffset += ip.getHeight() + padding();
        port.setX(sourceX);
        port.setY(sourceY + yOffset);
        yOffset += port.getHeight() + padding();
        user.setX(sourceX);
        user.setY(sourceY + yOffset);
        yOffset += user.getHeight() + padding();
        pass.setX(sourceX);
        pass.setY(sourceY + yOffset);
        yOffset += pass.getHeight() + padding();
        type.setX(sourceX);
        type.setY(sourceY + yOffset);
        yOffset += 20 + padding();
        double doubleWidth = wWidth / 2d - padding() / 2d;
        reset.setX(sourceX);
        reset.setY(sourceY + yOffset);
        apply.setX(sourceX + doubleWidth + padding());
        apply.setY(sourceY + yOffset);
        apply.setEnabled(canApply());
        yOffset += 20 + padding();
        widgetHeight = yOffset;

        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    boolean canApply() {
        String currentIp = this.ip.get();
        if (currentIp.isEmpty()) {
            return false;
        }
        String currentPort = this.port.get();
        try {
            int port = Integer.parseInt(currentPort);
            if (port < 0 || port > 0xFFFF) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Element child : children()) {
            child.mouseClicked(0, 0, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    public record Proxy(String address, int port, boolean socks4, String user, String pass) {

    }
}
