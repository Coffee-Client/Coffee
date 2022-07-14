/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.util;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;

public class ByteCounter implements DataOutput {
    private static final ByteCounter instance = new ByteCounter();
    private long c;

    public static ByteCounter instance() {
        return instance;
    }

    public long getSize() {
        return c;
    }

    public void reset() {
        c = 0;
    }

    @Override
    public void write(int b) {
        c++;
    }

    @Override
    public void write(byte[] b) {
        c += b.length;
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) {
        c += len;
    }

    @Override
    public void writeBoolean(boolean v) {
        c++;
    }

    @Override
    public void writeByte(int v) {
        c++;
    }

    @Override
    public void writeShort(int v) {
        c += 2;
    }

    @Override
    public void writeChar(int v) {
        c += 2;
    }

    @Override
    public void writeInt(int v) {
        c += 4;
    }

    @Override
    public void writeLong(long v) {
        c += 8;
    }

    @Override
    public void writeFloat(float v) {
        c += 4;
    }

    @Override
    public void writeDouble(double v) {
        c += 8;
    }

    @Override
    public void writeBytes(String s) {
        c += s.length();
    }

    @Override
    public void writeChars(String s) {
        c += s.length() * 2L;
    }

    @Override
    public void writeUTF(@NotNull String s) {
        c += getUTFLength(s) + 2;
    }

    long getUTFLength(String s) {
        long len = 0;
        for (char c : s.toCharArray()) {
            if (c >= 0x0001 && c <= 0x007F) {
                len++;
            } else if (c > 0x07FF) {
                len += 3;
            } else {
                len += 2;
            }
        }
        return len;
    }
}
