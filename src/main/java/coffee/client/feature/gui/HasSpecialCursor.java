/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.gui;

public interface HasSpecialCursor {
    long getCursor();

    boolean shouldApplyCustomCursor();
}
