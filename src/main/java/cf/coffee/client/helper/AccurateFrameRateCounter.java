/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper;

import java.util.ArrayList;
import java.util.List;

public class AccurateFrameRateCounter {
    public static final AccurateFrameRateCounter globalInstance = new AccurateFrameRateCounter();
    final List<Long> records = new ArrayList<>();

    public void recordFrame() {
        long c = System.currentTimeMillis();
        records.add(c);
        records.removeIf(aLong -> aLong + 1000 < c);
    }

    public int getFps() {
        return records.size();
    }
}
