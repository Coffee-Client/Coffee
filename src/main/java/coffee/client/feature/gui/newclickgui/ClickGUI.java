package coffee.client.feature.gui.newclickgui;

import coffee.client.feature.gui.newclickgui.element.CategoryDisplay;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.manager.ShaderManager;
import coffee.client.helper.util.Transitions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class ClickGUI extends AAScreen {
    boolean initialized = false;
    boolean closing = false;
    double progress = 0;
    @Override
    protected void initInternal() {
        closing = false;
        if (initialized) return;
        initialized = true;
        double x = 5;
        for (ModuleType value : ModuleType.values()) {
            CategoryDisplay gd = new CategoryDisplay(value, x, 5, 100);
            x += gd.getWidth() + 5;
            addChild(gd);
        }
    }

    @Override
    public void close() {
        closing = true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onFastTick() {
        double delta = 0.02;
        if (closing) delta *= -1;
        progress += delta;
        progress = MathHelper.clamp(progress, 0, 1);
        super.onFastTick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            closing = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (closing && progress == 0) {
            super.close();
        }
        double interpolated = Transitions.easeOutExpo(progress);
        ShaderManager.FROSTED_GLASS_BLUR.getEffect().setUniformValue("Progress", (float) interpolated);
        ShaderManager.FROSTED_GLASS_BLUR.render(delta);
        matrices.push();
        matrices.scale((float) interpolated, (float) interpolated, 1);
        super.render(matrices, mouseX, mouseY, delta);
        matrices.pop();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {

        super.renderInternal(stack, mouseX, mouseY, delta);
    }
}
