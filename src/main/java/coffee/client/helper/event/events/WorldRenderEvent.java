/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.event.events;

import coffee.client.helper.event.events.base.NonCancellableEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;

@RequiredArgsConstructor
public class WorldRenderEvent extends NonCancellableEvent {
    @Getter
    final MatrixStack contextStack;
}
