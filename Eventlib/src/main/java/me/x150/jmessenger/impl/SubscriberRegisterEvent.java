/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package me.x150.jmessenger.impl;

import me.x150.jmessenger.MessageManager;

/**
 * Emitted by {@link MessageManager}, when a new subscriber has been registered
 *
 * @param handler The new handler added to the subscriber list
 */
public record SubscriberRegisterEvent(MessageManager.Handler handler) {
}
