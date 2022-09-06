/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Profiler {
    private static final List<ProfilerEntry> entries = new ArrayList<>();
    private static ProfilerEntry nowRecording = null;

    public static void beginRecord() {
        if (nowRecording != null) {
            throw new IllegalStateException("Already recording");
        }
        StackWalker instance = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        StackWalker.StackFrame walk = instance.walk(stackFrameStream -> stackFrameStream.skip(1).findFirst().orElseThrow());
        long entryEnded = System.nanoTime();
        nowRecording = new ProfilerEntry(walk.getClassName() + "." + walk.getMethodName(), entryEnded, entryEnded, new Stack<>());
    }

    public static void recordSub(String name) {
        if (nowRecording == null) {
            throw new IllegalStateException("recordSub() called before beginRecord()");
        }
        long entryEnded = System.nanoTime();
        if (!nowRecording.subEntries.isEmpty()) {
            nowRecording.subEntries.peek().entryEnded = entryEnded;
        }
        nowRecording.subEntries.push(new ProfilerEntry(name, entryEnded, entryEnded, new Stack<>()));
    }

    public static void endSub() {
        if (nowRecording == null) {
            throw new IllegalStateException("endSub() called before beginRecord()");
        }
        if (nowRecording.subEntries.isEmpty()) {
            throw new IllegalStateException("endSub() called before recordSub()");
        }
        nowRecording.subEntries.peek().entryEnded = System.nanoTime();
    }

    public static void endRecord() {
        if (nowRecording == null) {
            throw new IllegalStateException("endRecord() called before beginRecord()");
        }
        long entryEnded = System.nanoTime();
        nowRecording.entryEnded = entryEnded;
        if (!nowRecording.subEntries.isEmpty()) {
            nowRecording.subEntries.peek().entryEnded = entryEnded;
        }
        entries.removeIf(profilerEntry -> profilerEntry.name.equals(nowRecording.name));
        entries.add(nowRecording);
        nowRecording = null;
    }

    public static List<ProfilerEntry> getEntries() {
        List<ProfilerEntry> entries1 = new ArrayList<>(entries);
        entries.clear();
        return entries1;
    }

    @Data
    @AllArgsConstructor
    public static class ProfilerEntry {
        String name;
        long entryMade, entryEnded;
        Stack<ProfilerEntry> subEntries;

        public long getDuration() {
            return entryEnded - entryMade;
        }
    }
}
