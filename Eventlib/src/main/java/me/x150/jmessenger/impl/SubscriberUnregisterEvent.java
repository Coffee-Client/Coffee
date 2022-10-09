/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package me.x150.jmessenger.impl;

import me.x150.jmessenger.MessageManager;

/**
 * Emitted by {@link MessageManager}, when an old event handler has been removed from the subscriber list
 *
 * @param handlerClass The class owning the handlers being removed
 */
public record SubscriberUnregisterEvent(Class<?> handlerClass) {
}
