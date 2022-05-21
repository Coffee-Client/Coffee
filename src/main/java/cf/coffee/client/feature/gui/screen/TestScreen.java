/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.screen;

import cf.coffee.client.feature.gui.element.impl.ButtonElement;
import cf.coffee.client.feature.gui.element.impl.FlexLayout;
import cf.coffee.client.feature.gui.element.impl.RoundTextFieldWidget;
import cf.coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class TestScreen extends AAScreen {

    @Override
    public void onFastTick() {
        super.onFastTick();

    }


    @Override
    protected void initInternal() {
        ButtonElement button1 = new ButtonElement(ButtonElement.STANDARD,0,0,100,20,"AMong us", () -> {
            System.out.println("btn 1 press");
        });
        ButtonElement button2 = new ButtonElement(ButtonElement.STANDARD,0,0,100,40,"AMong us 2", () -> {
            System.out.println("btn 2 press");
        });
        RoundTextFieldWidget tf1 = new RoundTextFieldWidget(0,0,70,20,"ee");
        ButtonElement button3 = new ButtonElement(ButtonElement.STANDARD,0,0,100,20,"eeee",() -> {
            tf1.set("");
        });
        tf1.changeListener = () -> {
            button3.setText(tf1.getText());
        };
        FlexLayout layout = new FlexLayout(FlexLayout.LayoutDirection.DOWN,5,5,100,50,5,button1,button2,tf1,button3);
        addChild(layout);
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        Renderer.R2D.renderQuad(stack,Color.WHITE,0,0,width,height);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

}
