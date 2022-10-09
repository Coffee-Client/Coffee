/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package me.x150.jmessenger.exception;

import me.x150.jmessenger.MessageManager;

/**
 * Thrown when a {@link MessageManager} received a subscription from a handler, which is already subscribed.
 */
public class SubscriberAlreadyRegisteredException extends RuntimeException {
    public SubscriberAlreadyRegisteredException(String cause) {
        super(cause);
    }
}
