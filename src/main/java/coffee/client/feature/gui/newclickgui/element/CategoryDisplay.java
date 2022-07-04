package coffee.client.feature.gui.newclickgui.element;

import coffee.client.feature.gui.element.Element;
import coffee.client.feature.gui.element.impl.FlexLayoutElement;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class CategoryDisplay extends Element {
    ModuleType type;
    List<Module> modules = new ArrayList<>();
    FlexLayoutElement layout;
    static final double maxHeight = 200;
    public CategoryDisplay(ModuleType type, double x, double y, double width) {
        super(x, y, width, 0);
        this.type = type;
        for (Module module : ModuleRegistry.getModules()) {
            if (module.getModuleType() == type) this.modules.add(module);
        }
        List<ModuleDisplay> displays = new ArrayList<>();
        for (Module module : modules) {
            displays.add(new ModuleDisplay(module,0,0,width));
        }
        layout = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.DOWN,x,y,width,maxHeight,3,displays.toArray(ModuleDisplay[]::new));

        // this.setHeight(calcHeight());
    }



    @Override
    public void tickAnimations() {
        layout.tickAnimations();
    }

    FontAdapter big = FontRenderers.getCustomSize(20);

    double headerHeight() {
        return big.getFontHeight()+3*2;
    }

    @Override
    public double getHeight() {
        return headerHeight()+super.getHeight();
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        big.drawCenteredString(stack,type.getName(),getPositionX()+getWidth()/2d,getPositionY()+headerHeight()/2d-big.getFontHeight()/2d,1f,1f,1f,1f);
        layout.setPositionX(getPositionX());
        layout.setPositionY(getPositionY()+headerHeight());
        layout.render(stack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return layout.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return layout.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        return layout.mouseDragged(x, y, xDelta, yDelta, button);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return layout.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return layout.keyPressed(keyCode, mods);
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return layout.keyReleased(keyCode, mods);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return layout.mouseScrolled(x, y, amount);
    }
}
