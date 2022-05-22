/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper;

import java.util.ArrayList;
import java.util.List;

public class AccurateFrameRateCounter {
    public static final AccurateFrameRateCounter globalInstance = new AccurateFrameRateCounter();
    final List<Long> records = new ArrayList<>();

    public void recordFrame() {
        long c = System.currentTimeMillis();
        records.add(c);
    }

    public int getFps() {
        records.removeIf(aLong -> aLong + 1000 < System.currentTimeMillis());
        return records.size();
    }
}
