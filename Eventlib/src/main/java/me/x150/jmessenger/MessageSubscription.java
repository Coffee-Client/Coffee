/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package me.x150.jmessenger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A subscription to a message type
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageSubscription {
    /**
     * The priority of this subscriber. The lower, the earlier in the subscriber stack it gets called
     *
     * @return The set priority, or 0 by default
     */
    int priority() default 0;
}
