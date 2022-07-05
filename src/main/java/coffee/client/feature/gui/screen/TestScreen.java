package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.screen.base.ClientScreen;
import coffee.client.helper.font.FontRenderers;
import net.minecraft.client.util.math.MatrixStack;

public class TestScreen extends ClientScreen {
    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        FontRenderers.getRenderer().drawString(stack, "this da content", 5, 5, 1, 1, 1, 1);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }
}
