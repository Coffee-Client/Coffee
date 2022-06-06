package coffee.client.mixin;

import coffee.client.feature.command.impl.SelfDestruct;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {
    @Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true, remap = false)
    private static void coffee_replaceClientModName(CallbackInfoReturnable<String> cir) {
        if (SelfDestruct.shouldSelfDestruct()) {
            cir.setReturnValue("vanilla");
        }
    }
}
