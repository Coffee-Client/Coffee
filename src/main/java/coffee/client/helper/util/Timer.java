/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
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

    public long getLastReset() {
        return lastReset;
    }
}
