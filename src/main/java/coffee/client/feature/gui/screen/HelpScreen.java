/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.element.impl.ButtonElement;
import coffee.client.feature.gui.element.impl.ClickableTextElement;
import coffee.client.feature.gui.element.impl.FlexLayoutElement;
import coffee.client.feature.gui.element.impl.SpacerElement;
import coffee.client.feature.gui.element.impl.TextElement;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.manager.ShaderManager;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;

import java.awt.Color;

public class HelpScreen extends AAScreen {
    Screen parent;
    public HelpScreen(Screen parent) {
        this.parent = parent;
    }
    static Color sub = new Color(0xCCCCCC);
    private static TextElement constructDefault(String text) {
        return new TextElement(FontRenderers.getRenderer(),text,sub,false,0,0);
    }
    @Override
    protected void initInternal() {
        TextElement title = new TextElement(FontRenderers.getCustomSize(30),"Coffee Manual", Color.WHITE,false,0,0);
        TextElement hello = new TextElement(FontRenderers.getRenderer(), "Welcome to Coffee!", Color.WHITE,false,0,0);
        SpacerElement spacer = new SpacerElement(0,0,0,10);
        TextElement intro = constructDefault("Coffee is an extremely versatile client, suited for a lot of things.");
        FlexLayoutElement element = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.DOWN,5,5,width-10,height-10,2,
            title,
            hello,
            spacer,
            intro,
            constructDefault("Tho, it might be hard to get used to it at first, these are some of the most asked questions."),
            new SpacerElement(0,0,0,5),
            constructDefault("To bind modules, use '.bind <Module name>', then press your desired key"),
            constructDefault("To configure the client, use the 'ClientConfig' module settings"),
            constructDefault("To search for modules, just type in the search term when the clickgui is open"),
            new SpacerElement(0, 0, 0, 5),
            new ClickableTextElement(0,0,"If you have more questions, click here to join the discord", FontRenderers.getRenderer(), () -> {
                Util.getOperatingSystem().open("https://discord.gg/yxEbQCdDus");
            }, 0xFFFFFF)
        );
        addChild(element);
        ButtonElement be = new ButtonElement(ButtonElement.DANGER,width-5-20,5,20,20,"X", this::close);
        addChild(be);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (parent != null) parent.render(matrices, -999, -999, delta);
        ShaderManager.BLUR.getEffect().setUniformValue("progress", (float) 1);
        ShaderManager.BLUR.render(delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
//
        Renderer.R2D.renderQuad(stack, new Color(0,0,0,100),0,0,width,height);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }
}
