/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element.impl.config;

import coffee.client.feature.config.ColorSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.gui.clickgui.element.Element;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

import static java.lang.Math.pow;

public class ColorSettingEditor extends ConfigBase<ColorSetting> {
    final DoubleSettingEditor red;
    final DoubleSettingEditor green;
    final DoubleSettingEditor blue;
    final DoubleSettingEditor alpha;
    boolean expanded = false;
    double expandProg = 1;

    public ColorSettingEditor(double x, double y, double width, ColorSetting configValue) {
        super(x, y, width, 0, configValue);
        red = new DoubleSettingEditor(x + 4, y, width - 4, new DoubleSetting((double) configValue.getValue().getRed(), "Red", "", 0, 0, 255, null) {
            @Override
            public Double getValue() {
                return (double) configValue.getValue().getRed();
            }

            @Override
            public void setValue(Double value) {
                super.setValue(value);
                configValue.setValue(Renderer.Util.modify(configValue.getValue(), (int) (value + 0), -1, -1, -1));
            }


        });
        green = new DoubleSettingEditor(x + 4,
                y + red.getHeight(),
                width - 4,
                new DoubleSetting((double) configValue.getValue().getGreen(), "Green", "", 0, 0, 255, null) {
                    @Override
                    public Double getValue() {
                        return (double) configValue.getValue().getGreen();
                    }

                    @Override
                    public void setValue(Double value) {
                        super.setValue(value);
                        configValue.setValue(Renderer.Util.modify(configValue.getValue(), -1, (int) (value + 0), -1, -1));
                    }


                }
        );
        blue = new DoubleSettingEditor(x + 4,
                y + red.getHeight() + green.getHeight(),
                width - 4,
                new DoubleSetting((double) configValue.getValue().getBlue(), "Blue", "", 0, 0, 255, null) {
                    @Override
                    public Double getValue() {
                        return (double) configValue.getValue().getBlue();
                    }

                    @Override
                    public void setValue(Double value) {
                        super.setValue(value);
                        configValue.setValue(Renderer.Util.modify(configValue.getValue(), -1, -1, (int) (value + 0), -1));
                    }


                }
        );
        alpha = new DoubleSettingEditor(x + 4,
                y + red.getHeight() + green.getHeight() + blue.getHeight(),
                width - 4,
                new DoubleSetting((double) configValue.getValue().getAlpha(), "Alpha", "", 0, 0, 255, null) {
                    @Override
                    public Double getValue() {
                        return (double) configValue.getValue().getAlpha();
                    }

                    @Override
                    public void setValue(Double value) {
                        super.setValue(value);
                        configValue.setValue(Renderer.Util.modify(configValue.getValue(), -1, -1, -1, (int) (value + 0)));
                    }


                }
        );
        //        this.height = red.getHeight()+green.getHeight()+blue.getHeight()+5;
    }

    double childHeight() {
        return red.getHeight() + green.getHeight() + blue.getHeight() + alpha.getHeight();
    }

    double headerHeight() {
        return FontRenderers.getRenderer().getMarginHeight() + 3;
    }

    Element[] getChildren() {
        return new Element[] { red, green, blue, alpha };
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        if (expanded) {
            for (Element child : getChildren()) {
                if (child.clicked(x, y, button)) {
                    return true;
                }
            }
        }
        if (x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + headerHeight()) {
            expanded = !expanded;
            return true;
        }
        return false;
    }

    @Override
    public double getHeight() {
        return headerHeight() + childHeight() * (this.expandProg < 0.5 ? 16 * this.expandProg * this.expandProg * this.expandProg * this.expandProg * this.expandProg : 1 - pow(-2 * this.expandProg + 2,
                5
        ) / 2) + 7;
    }

    @Override
    public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        for (Element child : getChildren()) {
            if (child.dragged(x, y, deltaX, deltaY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean released() {
        for (Element child : getChildren()) {
            child.released();
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        double expandProg = this.expandProg < 0.5 ? 16 * this.expandProg * this.expandProg * this.expandProg * this.expandProg * this.expandProg : 1 - pow(-2 * this.expandProg + 2,
                5
        ) / 2;
        Renderer.R2D.renderRoundedQuad(matrices, new Color(0, 0, 20, 60), x, y, x + width, y + getHeight() - 2, 5, 20);
        FontRenderers.getRenderer()
                .drawString(matrices, configValue.getName(), x + 2, y + headerHeight() / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d, 0xFFFFFF);
        double yOff = headerHeight();
        if (expandProg != 0) {
            ClipStack.globalInstance.addWindow(matrices, new Rectangle(x, y + headerHeight(), x + width, y + headerHeight() + childHeight() * expandProg));
            for (Element child : getChildren()) {
                child.setY(this.y + yOff);
                child.setX(this.x + 2);
                child.setWidth(width - 4);
                child.render(matrices, mouseX, mouseY, scrollBeingUsed);
                yOff += child.getHeight();
            }
            ClipStack.globalInstance.popWindow();
            //            yOff += 1;
        }

        Renderer.R2D.renderRoundedQuad(matrices,
                configValue.getValue(),
                x + 2,
                y + headerHeight() + childHeight() * expandProg,
                x + width - 3,
                y + headerHeight() + childHeight() * expandProg + 2,
                1,
                10
        );
    }

    @Override
    public void tickAnim() {
        double delta = 0.02;
        if (!expanded) {
            delta *= -1;
        }
        expandProg += delta;
        expandProg = MathHelper.clamp(expandProg, 0, 1);
        for (Element child : getChildren()) {
            child.tickAnim();
        }
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return false;
    }
}
