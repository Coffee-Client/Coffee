/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.element.impl.ButtonGroupElement;
import coffee.client.feature.gui.element.impl.FlexLayoutElement;
import coffee.client.feature.gui.element.impl.TextElement;
import coffee.client.feature.gui.element.impl.TextFieldElement;
import coffee.client.feature.gui.screen.base.CenterOverlayScreen;
import coffee.client.helper.font.FontRenderers;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ProxyManagerScreen extends CenterOverlayScreen {
    public static Proxy currentProxy = null;
    TextFieldElement ip;
    TextFieldElement port;
    boolean isSocks4 = true;

    public ProxyManagerScreen(Screen p) {
        super(p, "Proxy manager", "Manage your proxy connection");
    }

    @Override
    public void onFastTick() {
        super.onFastTick();
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
    protected void initInternal() {
        double oneWidth = 150;
        double padding = 5;
        double entireWidth = oneWidth * 2 + padding;
        TextElement mode = new TextElement(FontRenderers.getRenderer(),
                "Type: " + (isSocks4 ? "SOCKS4" : "SOCKS5"),
                Color.WHITE,
                false,
                0,
                0);
        TextElement conn = new TextElement(FontRenderers.getRenderer(),
                currentProxy == null ? "Not connected" : String.format("Connected to %s:%s",
                        currentProxy.address,
                        currentProxy.port),
                Color.WHITE,
                false,
                0,
                0);
        ip = new TextFieldElement(0, 0, oneWidth, 20, "IP");
        port = new TextFieldElement(0, 0, oneWidth, 20, "Port");
        TextFieldElement username = new TextFieldElement(0, 0, oneWidth, 20, "Username (opt.)");
        TextFieldElement password = new TextFieldElement(0, 0, oneWidth, 20, "Password (opt.)");
        ButtonGroupElement bg = new ButtonGroupElement(0,
                0,
                entireWidth,
                20,
                ButtonGroupElement.LayoutDirection.RIGHT,
                new ButtonGroupElement.ButtonEntry("Apply", () -> {
                    if (!canApply()) {
                        return;
                    }
                    Proxy p = new Proxy(ip.get(),
                            Integer.parseInt(port.get()),
                            isSocks4,
                            username.get(),
                            password.get());
                    currentProxy = p;
                    conn.setText(String.format("Connected to %s:%s", p.address, p.port));
                }),
                new ButtonGroupElement.ButtonEntry("Change type", () -> {
                    isSocks4 = !isSocks4;
                    mode.setText("Type: " + (isSocks4 ? "SOCKS4" : "SOCKS5"));
                }),
                new ButtonGroupElement.ButtonEntry("Reset", () -> {
                    currentProxy = null;
                    conn.setText("Not connected");
                }),
                new ButtonGroupElement.ButtonEntry("Close", this::close));
        FlexLayoutElement top = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.RIGHT, 0, 0, padding, ip, port);
        FlexLayoutElement middle = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.RIGHT,
                0,
                0,
                padding,
                username,
                password);
        FlexLayoutElement entire = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.DOWN,
                0,
                0,
                padding,
                mode,
                conn,
                top,
                middle,
                bg);
        addChild(entire);
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        //Renderer.R2D.renderQuad(stack, Color.WHITE, 0, 0, width, height);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    public record Proxy(String address, int port, boolean socks4, String user, String pass) {

    }

}
