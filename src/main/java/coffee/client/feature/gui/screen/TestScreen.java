package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class TestScreen extends AAScreen {
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Renderer.R2D.renderQuad(matrices, Color.WHITE, 10, 10, 100,100);

    }
}
