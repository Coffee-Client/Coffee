package coffee.client.feature.gui.element.impl;

import coffee.client.feature.gui.element.Element;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import lombok.AllArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ButtonGroupElement extends Element {
    List<Element> els = new ArrayList<>();
    LayoutDirection dir;

    public ButtonGroupElement(double x, double y, double width, double height, LayoutDirection direction, ButtonEntry... btns) {
        super(x, y, width, height);
        this.dir = direction;
        createButtons(btns);
    }

    void createButtons(ButtonEntry[] e) {
        double widthForOneButton = getWidth() / e.length;
        double heightForOneButton = getHeight() / e.length;
        for (int i = 0; i < e.length; i++) {
            boolean isFirst = i == 0;
            boolean isLast = i == (e.length - 1);
            ButtonEntry be = e[i];
            double rad1, rad2, rad3, rad4;
            if (isFirst) {
                rad1 = dir.rad1b;
                rad2 = dir.rad2b;
                rad3 = dir.rad3b;
                rad4 = dir.rad4b;
            } else if (isLast) {
                rad1 = dir.rad1e;
                rad2 = dir.rad2e;
                rad3 = dir.rad3e;
                rad4 = dir.rad4e;
            } else {
                rad1 = 0;
                rad2 = 0;
                rad3 = 0;
                rad4 = 0;
            }
            SingleButton btn = new SingleButton(0,
                    0,
                    dir == LayoutDirection.RIGHT ? widthForOneButton : width,
                    dir == LayoutDirection.RIGHT ? height : heightForOneButton,
                    be.content,
                    be.onClicked,
                    rad1 * 5,
                    rad2 * 5,
                    rad3 * 5,
                    rad4 * 5,
                    new Color(30, 30, 30),
                    new Color(50, 50, 50));
            els.add(btn);
        }
    }

    @Override
    public void tickAnimations() {

    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        double x = 0;
        double y = 0;
        for (Element el : els) {
            el.setPositionX(getPositionX() + x * dir.mulX);
            el.setPositionY(getPositionY() + y * dir.mulY);
            el.render(stack, mouseX, mouseY);
            x += el.getWidth();
            y += el.getHeight();
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        for (Element el : els) {
            if (el.mouseClicked(x, y, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        return false;
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return false;
    }

    @AllArgsConstructor
    public enum LayoutDirection {
        DOWN(0, 1, 1, 1, 0, 0, 0, 0, 1, 1), RIGHT(1, 0, 1, 0, 1, 0, 0, 1, 0, 1);
        final int mulX, mulY;
        final double rad1b, rad2b, rad3b, rad4b;
        final double rad1e, rad2e, rad3e, rad4e;
    }

    public record ButtonEntry(String content, Runnable onClicked) {
    }

    static class SingleButton extends Element {
        String t;
        Runnable r;
        double r1, r2, r3, r4;
        Color c, sel;

        public SingleButton(double x, double y, double width, double height, String text, Runnable r, double rad1, double rad2, double rad3, double rad4, Color c, Color sel) {
            super(x, y, width, height);
            this.t = text;
            this.r = r;
            this.r1 = rad1;
            this.r2 = rad2;
            this.r3 = rad3;
            this.r4 = rad4;
            this.c = c;
            this.sel = sel;
        }

        @Override
        public void tickAnimations() {

        }

        @Override
        public void render(MatrixStack stack, double mouseX, double mouseY) {
            boolean mouseOver = inBounds(mouseX, mouseY);
            Color c = mouseOver ? this.sel : this.c;
            if (r1 != 0 || r2 != 0 || r3 != 0 || r4 != 0) {
                Renderer.R2D.renderRoundedQuad(stack,
                        c,
                        getPositionX(),
                        getPositionY(),
                        getPositionX() + getWidth(),
                        getPositionY() + getHeight(),
                        r1,
                        r2,
                        r3,
                        r4,
                        20);
            } else {
                Renderer.R2D.renderQuad(stack,
                        c,
                        getPositionX(),
                        getPositionY(),
                        getPositionX() + getWidth(),
                        getPositionY() + getHeight());
            }
            FontRenderers.getRenderer()
                    .drawCenteredString(stack,
                            t,
                            getPositionX() + getWidth() / 2d,
                            getPositionY() + getHeight() / 2d - FontRenderers.getRenderer().getFontHeight() / 2d,
                            1f,
                            1f,
                            1f,
                            1f);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (inBounds(x, y) && button == 0) {
                r.run();
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            return false;
        }

        @Override
        public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
            return false;
        }

        @Override
        public boolean charTyped(char c, int mods) {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int mods) {
            return false;
        }

        @Override
        public boolean keyReleased(int keyCode, int mods) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double x, double y, double amount) {
            return false;
        }
    }
}
