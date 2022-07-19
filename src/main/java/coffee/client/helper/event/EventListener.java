/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {
    EventType value();
}
