/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.HasSpecialCursor;
import coffee.client.feature.gui.element.Element;
import coffee.client.feature.gui.element.impl.ButtonElement;
import coffee.client.feature.gui.element.impl.ButtonGroupElement;
import coffee.client.feature.gui.element.impl.ColorEditorElement;
import coffee.client.feature.gui.element.impl.FlexLayoutElement;
import coffee.client.feature.gui.element.impl.TextFieldElement;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.feature.gui.screen.base.CenterOverlayScreen;
import coffee.client.feature.module.impl.render.Waypoints;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.impl.QuickFontAdapter;
import coffee.client.helper.render.Cursor;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WaypointEditScreen extends AAScreen {
    AddButton adb = new AddButton(0, 0, 60, 60, this::add);
    FlexLayoutElement el;
    boolean resized = false;

    void add() {
        Waypoints.Waypoint wp = new Waypoints.Waypoint();
        wp.setColor(Color.WHITE);
        wp.setName("New waypoint");
        wp.setPosition(CoffeeMain.client.player.getPos());
        Waypoints.waypoints.add(wp);
        init();
    }

    @Override
    protected void initInternal() {
        List<Element> els = new ArrayList<>();
        for (Waypoints.Waypoint waypoint : Waypoints.waypoints) {
            els.add(new WaypointVis(0, 0, waypoint));
        }
        els.add(adb);
        if (el == null || resized) {
            el = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.RIGHT, 5, 5, width - 10, height - 10, 5, els.toArray(Element[]::new));
        } else {
            el.setElements(els);
        }
        resized = false;
        addChild(el);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        resized = false;
        super.resize(client, width, height);
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        Renderer.R2D.renderQuad(stack, new Color(0, 0, 0, 150), 0, 0, width, height);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    void renderPlusIcon(MatrixStack stack, double x, double y, double size) {
        Renderer.R2D.renderCircle(stack, new Color(11, 145, 225), x, y, size, 60);
        Renderer.R2D.renderRoundedQuad(stack, Color.WHITE, x - size / 2d, y - 1, x + size / 2d, y + 1, 1, 5);
        Renderer.R2D.renderRoundedQuad(stack, Color.WHITE, x - 1, y - size / 2d, x + 1, y + size / 2d, 1, 5);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    static class EditScreen extends CenterOverlayScreen {
        Waypoints.Waypoint wp;

        public EditScreen(Screen parent, Waypoints.Waypoint wp) {
            super(parent, "Waypoint editor", "Edit waypoints");
            this.wp = wp;
        }

        @Override
        protected void initInternal() {
            super.initInternal();
            ButtonElement save = new ButtonElement(ButtonElement.SUCCESS, 0, 0, 200, 20, "Save", this::close);
            TextFieldElement textFieldElement = new TextFieldElement(0, 0, 200, 20, "Name");
            textFieldElement.set(wp.getName());
            textFieldElement.setChangeListener(() -> wp.setName(textFieldElement.get()));
            TextFieldElement position = new TextFieldElement(0, 0, 200, 20, "Position");
            position.set(String.format(Locale.ENGLISH, "%.2f %.2f %.2f", wp.getPosition().x, wp.getPosition().y, wp.getPosition().z));
            position.setChangeListener(() -> {
                String content = position.get();
                try {
                    String[] spl = content.split(" ");
                    double x = Double.parseDouble(spl[0]);
                    double y = Double.parseDouble(spl[1]);
                    double z = Double.parseDouble(spl[2]);
                    wp.setPosition(new Vec3d(x, y, z));
                    position.setInvalid(false);
                } catch (Exception e) {
                    position.setInvalid(true);
                }
            });
            ColorEditorElement ce = new ColorEditorElement(0, 0, 200, 100, wp.getColor());
            ce.setOnChange(color -> wp.setColor(color));
            FlexLayoutElement flexLayoutElement = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.DOWN, 0, 0, 5, textFieldElement, position, ce, save);
            addChild(flexLayoutElement);
        }
    }

    class AddButton extends Element implements HasSpecialCursor {
        double expand = 0;
        Runnable onClick;
        boolean mouseOver = false;

        public AddButton(double x, double y, double width, double height, Runnable onClick) {
            super(x, y, width, height);
            this.onClick = onClick;
        }

        @Override
        public void tickAnimations() {
            double delta = 0.04;
            if (!mouseOver) {
                delta *= -1;
            }
            expand += delta;
            expand = MathHelper.clamp(expand, 0, 1);
        }

        @Override
        public void render(MatrixStack stack, double mouseX, double mouseY) {
            mouseOver = inBounds(mouseX, mouseY);
            double expand = Transitions.easeOutExpo(this.expand);
            Renderer.R2D.renderRoundedQuad(stack,
                new Color(20, 20, 20),
                getPositionX(),
                getPositionY(),
                getPositionX() + getWidth(),
                getPositionY() + getHeight(),
                5,
                20);
            renderPlusIcon(stack, getPositionX() + getWidth() / 2d, getPositionY() + getHeight() / 2d, 10 + 2 * expand);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (inBounds(x, y) && button == 0) {
                onClick.run();
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

        @Override
        public long getCursor() {
            return Cursor.CLICK;
        }

        @Override
        public boolean shouldApplyCustomCursor() {
            return mouseOver;
        }
    }

    class WaypointVis extends Element {
        Waypoints.Waypoint wayp;
        ButtonGroupElement actions;

        public WaypointVis(double x, double y, Waypoints.Waypoint wayp) {
            super(x, y, 100, 60);
            this.wayp = wayp;
            this.actions = new ButtonGroupElement(0,
                0,
                getWidth() - 10,
                20,
                ButtonGroupElement.LayoutDirection.RIGHT,
                new ButtonGroupElement.ButtonEntry("Edit", this::edit),
                new ButtonGroupElement.ButtonEntry("Delete", this::delete));
        }

        void edit() {
            client.setScreen(new EditScreen(WaypointEditScreen.this, wayp));
        }

        void delete() {
            Waypoints.waypoints.remove(wayp);
            init();
        }

        @Override
        public void tickAnimations() {

        }

        @Override
        public void render(MatrixStack stack, double mouseX, double mouseY) {
            this.actions.setPositionX(getPositionX() + 5);
            this.actions.setPositionY(getPositionY() + getHeight() - 20 - 5);
            Renderer.R2D.renderRoundedQuad(stack,
                new Color(20, 20, 20),
                getPositionX(),
                getPositionY(),
                getPositionX() + getWidth(),
                getPositionY() + getHeight(),
                5,
                20);
            QuickFontAdapter customSize = FontRenderers.getCustomSize(20);
            String t = Utils.capAtLength(wayp.getName(), getWidth() - 10, customSize);
            customSize.drawString(stack, t, getPositionX() + 5, getPositionY() + 5, 0xFFFFFF);
            this.actions.render(stack, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return this.actions.mouseClicked(x, y, button);
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
