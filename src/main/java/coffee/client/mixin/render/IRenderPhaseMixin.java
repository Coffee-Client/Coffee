package coffee.client.mixin.render;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface IRenderPhaseMixin {
    @Accessor("name")
    String getName();
}
