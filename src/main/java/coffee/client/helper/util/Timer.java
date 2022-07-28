/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.util;

public class Timer {
    long lastReset;

    public Timer() {
        reset();
    }

    public void reset() {
        lastReset = System.currentTimeMillis();
    }

    public boolean hasExpired(long timeout) {
        return System.currentTimeMillis() - lastReset > timeout;
    }

}
