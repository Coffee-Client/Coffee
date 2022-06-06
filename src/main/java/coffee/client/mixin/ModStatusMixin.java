package coffee.client.mixin;

import coffee.client.feature.command.impl.SelfDestruct;
import net.minecraft.util.ModStatus;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(ModStatus.class)
public class ModStatusMixin {
    @Inject(method = "isModded", at = @At("HEAD"), cancellable = true)
    void coffee_replaceModdedStatus(CallbackInfoReturnable<Boolean> cir) {
        if (SelfDestruct.shouldSelfDestruct()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "combine", at = @At(value = "FIELD", target = "Lnet/minecraft/util/ModStatus;confidence:Lnet/minecraft/util/ModStatus$Confidence;", ordinal = 0))
    ModStatus.Confidence coffee_replaceOurConfidence(ModStatus instance) {
        return SelfDestruct.shouldSelfDestruct() ? ModStatus.Confidence.PROBABLY_NOT : instance.confidence();
    }

    @Redirect(method = "getMessage", at = @At(value = "FIELD", target = "Lnet/minecraft/util/ModStatus;confidence:Lnet/minecraft/util/ModStatus$Confidence;", ordinal = 0))
    ModStatus.Confidence coffee_replaceOurConfidence1(ModStatus instance) {
        return SelfDestruct.shouldSelfDestruct() ? ModStatus.Confidence.PROBABLY_NOT : instance.confidence();
    }

    @Inject(method = "confidence", at = @At("HEAD"), cancellable = true)
    void coffee_replaceConfidence(CallbackInfoReturnable<ModStatus.Confidence> cir) {
        if (SelfDestruct.shouldSelfDestruct()) {
            cir.setReturnValue(ModStatus.Confidence.PROBABLY_NOT);
        }
    }
}
