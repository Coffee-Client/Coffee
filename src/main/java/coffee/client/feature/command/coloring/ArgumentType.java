/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.coloring;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.Color;

public enum ArgumentType {
    STRING(new Color(0x55FF55), String.class),
    NUMBER(new Color(0x009DFF), Integer.class, Double.class, Float.class, Long.class),
    PLAYER(new Color(0xFF9900), PlayerEntity.class, AbstractClientPlayerEntity.class, LivingEntity.class);
    final Color color;
    final Class<?>[] appliesTo;

    ArgumentType(Color color, Class<?>... appliesToClass) {
        this.color = color;
        this.appliesTo = appliesToClass;
    }

    public Color getColor() {
        return color;
    }

    public Class<?>[] getAppliesTo() {
        return appliesTo;
    }
}
