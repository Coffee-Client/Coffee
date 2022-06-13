/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.notifications.Notification;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.MouseEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class FakeHacker extends Module {
    PlayerEntity target = null;

    public FakeHacker() {
        super("FakeHacker", "Makes it seem like another user is hacking", ModuleType.RENDER);
        Events.registerEventHandler(EventType.MOUSE_EVENT, event -> {
            if (!this.isEnabled()) {
                return;
            }
            if (CoffeeMain.client.player == null || CoffeeMain.client.world == null) {
                return;
            }
            if (CoffeeMain.client.currentScreen != null) {
                return;
            }
            MouseEvent me = (MouseEvent) event;
            if (me.getAction() == 1 && me.getButton() == 2) {
                HitResult hr = CoffeeMain.client.crosshairTarget;
                if (hr instanceof EntityHitResult ehr && ehr.getEntity() instanceof PlayerEntity pe) {
                    target = pe;
                }
            }
        });
    }

    @Override
    public void tick() {
        if (target != null) {
            Iterable<Entity> entities = Objects.requireNonNull(CoffeeMain.client.world).getEntities();
            List<Entity> entities1 = new ArrayList<>(StreamSupport.stream(entities.spliterator(), false).toList());
            Collections.shuffle(entities1);
            for (Entity entity : entities1) {
                if (entity.equals(target)) {
                    continue;
                }
                if (entity.isAttackable() && entity.distanceTo(target) < 4) {
                    target.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entity.getPos().add(0, entity.getHeight() / 2, 0));
                    target.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }

    @Override
    public void enable() {
        target = null;
        Notification.create(6000, "", true, Notification.Type.INFO, "Middle click a player to select them");
    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return target == null ? null : target.getEntityName();
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }
}
