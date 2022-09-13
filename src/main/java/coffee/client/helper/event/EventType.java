/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.event;

import coffee.client.helper.event.events.*;
import coffee.client.helper.event.events.base.Event;
import coffee.client.helper.event.events.base.NonCancellableEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventType {
    PACKET_SEND(PacketEvent.class, false),
    PACKET_RECEIVE(PacketEvent.class, false),
    ENTITY_RENDER(EntityRenderEvent.class, false),
    BLOCK_ENTITY_RENDER(BlockEntityRenderEvent.class, false),
    BLOCK_RENDER(BlockRenderEvent.class, false),
    MOUSE_EVENT(MouseEvent.class, false),
    LORE_QUERY(LoreQueryEvent.class, false),
    CONFIG_SAVE(NonCancellableEvent.class, true),
    NOCLIP_QUERY(PlayerNoClipQueryEvent.class, false),
    KEYBOARD(KeyboardEvent.class, false),
    POST_INIT(NonCancellableEvent.class, true),
    HUD_RENDER(NonCancellableEvent.class, false),
    GAME_EXIT(NonCancellableEvent.class, true),
    SHOULD_RENDER_CHUNK(ChunkRenderQueryEvent.class, false),
    WORLD_RENDER(WorldRenderEvent.class, false),
    SNEAK_QUERY(SneakQueryEvent.class, false);
    @Getter
    private final Class<? extends Event> expectedType;
    @Getter
    private final boolean shouldStayRegisteredForModules;
}
