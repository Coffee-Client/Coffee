/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.event.events;

import coffee.client.helper.event.events.base.NonCancellableEvent;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerNoClipQueryEvent extends NonCancellableEvent {

    final PlayerEntity player;
    NoClipState state = NoClipState.UNSET;

    public PlayerNoClipQueryEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public NoClipState getNoClipState() {
        return state;
    }

    public void setNoClipState(NoClipState state) {
        this.state = state;
    }

    public boolean getNoClip() {
        if (state == NoClipState.UNSET) {
            return player.isSpectator();
        } else {
            return state == NoClipState.ACTIVE;
        }
    }

    public enum NoClipState {
        UNSET, ACTIVE, INACTIVE
    }
}
