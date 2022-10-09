/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.misc.AntiCrash;
import coffee.client.feature.module.impl.render.BlockHighlighting;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.ChunkRenderQuery;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.stream.StreamSupport;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean coffee_renderEverything(boolean spectator) {
        ChunkRenderQuery query = new ChunkRenderQuery();
        EventSystem.manager.send(query);
        return query.isModified() ? query.isShouldRender() : spectator; // only submit our value if we have a reason to
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;long2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;"))
    ObjectSet<Long2ObjectMap.Entry<SortedSet<BlockBreakingInfo>>> coffee_highlightBlocks(Long2ObjectMap<SortedSet<BlockBreakingInfo>> instance, MatrixStack matrices) {
        BlockHighlighting bbr = ModuleRegistry.getByClass(BlockHighlighting.class);
        if (bbr.isEnabled()) {
            return ObjectSet.of();
        }
        return instance.long2ObjectEntrySet();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getEntities()Ljava/lang/Iterable;"))
    Iterable<Entity> coffee_filterEntities(ClientWorld instance) {
        AntiCrash inst = ModuleRegistry.getByClass(AntiCrash.class);
        Iterable<Entity> entities = instance.getEntities();
        if (!inst.isEnabled() || !inst.getHideMassEntities().getValue()) {
            return entities;
        }
        Object2IntMap<EntityType<?>> entityTypeCount = new Object2IntArrayMap<>();
        return StreamSupport.stream(entities.spliterator(), false)
            .sorted(Comparator.comparingDouble(value -> value.distanceTo(CoffeeMain.client.cameraEntity)))
            .filter(entity -> {
                int oldCount = entityTypeCount.getOrDefault(entity.getType(), 0);
                entityTypeCount.put(entity.getType(), oldCount + 1);
                return oldCount < inst.getMassEntityAmount().getValue();
            })
            .toList();
    }
}
