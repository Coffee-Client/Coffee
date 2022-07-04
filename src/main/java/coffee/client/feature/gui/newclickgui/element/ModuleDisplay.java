package coffee.client.feature.gui.newclickgui.element;

import coffee.client.feature.gui.element.Element;
import coffee.client.feature.module.Module;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ModuleDisplay extends Element {
    Module module;
    public ModuleDisplay(Module module, double x, double y, double width) {
        super(x, y, width, 20);
        this.module = module;
    }

    @Override
    public void tickAnimations() {

    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        Renderer.R2D.renderRoundedQuad(stack,module.isEnabled()?new Color(0x607D8B):new Color(0x37474F),getPositionX(),getPositionY(),getPositionX()+getWidth(),getPositionY()+getHeight(),5,20);
        FontRenderers.getRenderer().drawCenteredString(stack,module.getName(),getPositionX()+getWidth()/2d,getPositionY()+getHeight()/2d-FontRenderers.getRenderer()
                .getFontHeight()/2d,1f,1f,1f,1f);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (inBounds(x, y) && button == 0) {
            module.toggle();
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
