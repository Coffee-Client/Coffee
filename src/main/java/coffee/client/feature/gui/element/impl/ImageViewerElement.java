package coffee.client.feature.gui.element.impl;

import coffee.client.feature.gui.element.Element;
import coffee.client.helper.GameTexture;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ImageViewerElement extends Element {
    Identifier tex;

    public ImageViewerElement(double x, double y, GameTexture texture) {
        super(x, y, texture.getDimensions().getWidth(), texture.getDimensions().getHeight());
        this.tex = texture.getWhere();
    }

    public ImageViewerElement(double x, double y, GameTexture texture, double width) {
        super(x, y, texture.getDimensions().getWidth(), texture.getDimensions().getHeight());
        Rectangle scaled = getScaledDimensions(texture.getDimensions(), width);
        setWidth(scaled.getWidth());
        setHeight(scaled.getHeight());
        this.tex = texture.getWhere();
    }

    public ImageViewerElement(double x, double y, double height, GameTexture texture) {
        super(x, y, texture.getDimensions().getWidth(), texture.getDimensions().getHeight());
        Rectangle scaled = getScaledDimensionsWithHeight(texture.getDimensions(), height);
        setWidth(scaled.getWidth());
        setHeight(scaled.getHeight());
        this.tex = texture.getWhere();
    }

    public ImageViewerElement(double x, double y, Identifier texture, double width, double height) {
        super(x, y, width, height);
        this.tex = texture;
    }

    private Rectangle getScaledDimensions(Rectangle orig, double newWidth) {
        double mulFac = newWidth / orig.getWidth();
        double newHeight = orig.getHeight() * mulFac;
        return new Rectangle(orig.getX(), orig.getY(), orig.getX() + newWidth, orig.getY() + newHeight);
    }

    private Rectangle getScaledDimensionsWithHeight(Rectangle orig, double newHeight) {
        double mulFac = newHeight / orig.getHeight();
        double newWidth = orig.getWidth() * mulFac;
        return new Rectangle(orig.getX(), orig.getY(), orig.getX() + newWidth, orig.getY() + newHeight);
    }

    @Override
    public void tickAnimations() {

    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        RenderSystem.setShaderTexture(0, tex);
        Renderer.R2D.renderTexture(stack, getPositionX(), getPositionY(), getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
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
