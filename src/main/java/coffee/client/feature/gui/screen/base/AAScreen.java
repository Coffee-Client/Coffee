package coffee.client.feature.gui.screen.base;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.element.Element;
import coffee.client.helper.render.MSAAFramebuffer;
import lombok.Getter;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class AAScreen extends Screen implements FastTickable {
    final int samples;

    @Getter
    private final CopyOnWriteArrayList<Element> elements = new CopyOnWriteArrayList<>();

    public AAScreen(int samples) {
        super(Text.of(""));
        this.samples = samples;
    }

    public AAScreen() {
        this(MSAAFramebuffer.MAX_SAMPLES);
    }

    @Override
    public void onFastTick() {
        for (Element element : getElements()) {
            element.tickAnimations();
        }
    }

    @Override
    protected final void init() {
        elements.clear();
        initInternal();
    }

    protected void initInternal() {

    }

    public void addChild(Element element) {
        elements.add(element);
    }

    public void removeChild(Element element) {
        elements.remove(element);
    }

    @Override
    protected <T extends Drawable> T addDrawable(T drawable) {
        throw new NotImplementedException("Use AAScreen#addChild or #removeChild instead of the legacy screen api");
    }

    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        for (Element element : getElements()) {
            element.render(stack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return iterateOverChildren(element -> element.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return iterateOverChildren(element -> element.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return iterateOverChildren(element -> element.mouseDragged(mouseX, mouseY, deltaX, deltaY, button));
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return iterateOverChildren(element -> element.charTyped(chr, modifiers));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            CoffeeMain.client.setScreen(null);
            return true;
        }
        return iterateOverChildren(element -> element.keyPressed(keyCode, modifiers));
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return iterateOverChildren(element -> element.keyReleased(keyCode, modifiers));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return iterateOverChildren(element -> element.mouseScrolled(mouseX, mouseY, amount));
    }

    private boolean iterateOverChildren(Function<Element, Boolean> supp) {
        for (Element element : getElements()) {
            if (supp.apply(element)) return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        CoffeeMain.client.keyboard.setRepeatEvents(true);
        if (samples != -1) {
            if (!MSAAFramebuffer.framebufferInUse()) {
                MSAAFramebuffer.use(samples, () -> renderInternal(matrices, mouseX, mouseY, delta));
            } else {
                renderInternal(matrices, mouseX, mouseY, delta);
            }
        } else {
            renderInternal(matrices, mouseX, mouseY, delta);
        }
    }
}
