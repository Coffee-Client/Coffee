/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.event.impl;

import coffee.client.helper.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.PlayerEntity;

@AllArgsConstructor
public class NoclipQueryEvent extends Event {
    @Getter
    final PlayerEntity player;
    @Getter
    @Setter
    boolean shouldNoclip;
}
