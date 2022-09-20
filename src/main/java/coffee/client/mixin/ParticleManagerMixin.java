/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.feature.module.impl.misc.AntiCrash;
import coffee.client.mixinUtil.ParticleManagerDuck;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin implements ParticleManagerDuck {
    @Shadow
    @Final
    private Map<ParticleTextureSheet, Queue<Particle>> particles;

    @Shadow
    @Final
    private Queue<Particle> newParticles;

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    void coffee_tick(CallbackInfo ci) {
        AntiCrash ac = AntiCrash.instance();
        if (ac.isEnabled()) {
            if (ac.getCapParticles().getValue()) {
                int max = (int) Math.floor(ac.getParticleMax().getValue());
                int totalParticles = this.particles.values().stream().mapToInt(Collection::size).sum() + this.newParticles.size();
                if (totalParticles >= max) {
                    ci.cancel();
                }
            }
        }
    }

    @Override
    public int getTotalParticles() {
        return this.particles.values().stream().mapToInt(Collection::size).sum() + this.newParticles.size();
    }
}
