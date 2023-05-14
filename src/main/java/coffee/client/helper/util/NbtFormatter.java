/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NbtFormatter implements NbtElementVisitor {
    private static final Map<String, List<String>> ENTRY_ORDER_OVERRIDES = Util.make(Maps.newHashMap(), (map) -> {
        map.put("{}", Lists.newArrayList("DataVersion", "author", "size", "data", "entities", "palette", "palettes"));
        map.put("{}.data.[].{}", Lists.newArrayList("pos", "state", "nbt"));
        map.put("{}.entities.[].{}", Lists.newArrayList("blockPos", "pos"));
    });
    private static final Set<String> IGNORED_PATHS = Sets.newHashSet("{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}");
    private static final Pattern SIMPLE_NAME = Pattern.compile("[A-Za-z\\d._+-]+");
    private static final String KEY_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ENTRY_SEPARATOR = String.valueOf(',');
    private static final int NAME_COLOR = 0x55FFFF;
    private static final int STRING_COLOR = 0x55FF55;
    private static final int NUMBER_COLOR = 0xFFAA00;
    private static final int TYPE_SUFFIX_COLOR = 0xFF5555;
    private final String prefix;
    private final int indentationLevel;
    private final List<String> pathParts;
    private RGBColorText result;

    public NbtFormatter(String prefix, int indentationLevel, List<String> pathParts) {
        this.prefix = prefix;
        this.indentationLevel = indentationLevel;
        this.pathParts = pathParts;
    }

    protected static String escapeName(String name) {
        return SIMPLE_NAME.matcher(name).matches() ? name : NbtString.escape(name);
    }

    public RGBColorText apply(NbtElement element) {
        element.accept(this);
        return this.result;
    }

    public void visitString(NbtString element) {
        this.result = new RGBColorText(NbtString.escape(element.asString()), STRING_COLOR);
    }

    public void visitByte(NbtByte element) {
        this.result = new RGBColorText(element.numberValue(), NUMBER_COLOR).append("b", TYPE_SUFFIX_COLOR);
    }

    public void visitShort(NbtShort element) {
        this.result = new RGBColorText(element.numberValue(), NUMBER_COLOR).append("s", TYPE_SUFFIX_COLOR);
    }

    public void visitInt(NbtInt element) {
        this.result = new RGBColorText(element.numberValue(), NUMBER_COLOR);
    }

    public void visitLong(NbtLong element) {
        this.result = new RGBColorText(element.numberValue(), NUMBER_COLOR).append("L", TYPE_SUFFIX_COLOR);
    }

    public void visitFloat(NbtFloat element) {
        this.result = new RGBColorText(element.floatValue(), NUMBER_COLOR).append("f", TYPE_SUFFIX_COLOR);
    }

    public void visitDouble(NbtDouble element) {
        this.result = new RGBColorText(element.doubleValue(), NUMBER_COLOR).append("d", TYPE_SUFFIX_COLOR);
    }

    public void visitByteArray(NbtByteArray element) {
        RGBColorText stringBuilder = (new RGBColorText("[")).append("B", TYPE_SUFFIX_COLOR).append(";");
        byte[] bs = element.getByteArray();

        for (int i = 0; i < bs.length; ++i) {
            stringBuilder.append(" ").append(String.valueOf(bs[i]), NUMBER_COLOR).append("B", TYPE_SUFFIX_COLOR);
            if (i != bs.length - 1) {
                stringBuilder.append(ENTRY_SEPARATOR);
            }
        }

        stringBuilder.append("]");
        this.result = new RGBColorText(stringBuilder);
    }

    public void visitIntArray(NbtIntArray element) {
        RGBColorText stringBuilder = (new RGBColorText("[")).append("I", TYPE_SUFFIX_COLOR).append(";");
        int[] is = element.getIntArray();

        for (int i = 0; i < is.length; ++i) {
            stringBuilder.append(" ").append(String.valueOf(is[i]), NUMBER_COLOR);
            if (i != is.length - 1) {
                stringBuilder.append(ENTRY_SEPARATOR);
            }
        }

        stringBuilder.append("]");
        this.result = new RGBColorText(stringBuilder);
    }

    public void visitLongArray(NbtLongArray element) {
        RGBColorText stringBuilder = (new RGBColorText("[")).append("L", TYPE_SUFFIX_COLOR).append(";");
        long[] ls = element.getLongArray();

        for (int i = 0; i < ls.length; ++i) {
            stringBuilder.append(" ").append(String.valueOf(ls[i]), NUMBER_COLOR).append("L", TYPE_SUFFIX_COLOR);
            if (i != ls.length - 1) {
                stringBuilder.append(ENTRY_SEPARATOR);
            }
        }

        stringBuilder.append("]");
        this.result = new RGBColorText(stringBuilder);
    }

    public void visitList(NbtList element) {
        if (element.isEmpty()) {
            this.result = new RGBColorText("[]");
        } else {
            RGBColorText stringBuilder = new RGBColorText("[");
            this.pushPathPart("[]");
            String string = IGNORED_PATHS.contains(this.joinPath()) ? "" : this.prefix;
            if (!string.isEmpty()) {
                stringBuilder.append("\n");
            }

            for (int i = 0; i < element.size(); ++i) {
                stringBuilder.append(Strings.repeat(string, this.indentationLevel + 1));
                stringBuilder.append((new NbtFormatter(string, this.indentationLevel + 1, this.pathParts)).apply(element.get(i)));
                if (i != element.size() - 1) {
                    stringBuilder.append(ENTRY_SEPARATOR).append(string.isEmpty() ? " " : "\n");
                }
            }

            if (!string.isEmpty()) {
                stringBuilder.append("\n").append(Strings.repeat(string, this.indentationLevel));
            }

            stringBuilder.append("]");
            this.result = new RGBColorText(stringBuilder);
            this.popPathPart();
        }
    }

    public void visitCompound(NbtCompound compound) {
        if (compound.isEmpty()) {
            this.result = new RGBColorText("{}");
        } else {
            RGBColorText stringBuilder = new RGBColorText("{");
            this.pushPathPart("{}");
            String string = IGNORED_PATHS.contains(this.joinPath()) ? "" : this.prefix;
            if (!string.isEmpty()) {
                stringBuilder.append("\n");
            }

            Collection<String> collection = this.getSortedNames(compound);
            Iterator<String> iterator = collection.iterator();

            while (iterator.hasNext()) {
                String string2 = iterator.next();
                NbtElement nbtElement = compound.get(string2);
                this.pushPathPart(string2);
                stringBuilder.append(Strings.repeat(string, this.indentationLevel + 1))
                             .append(escapeName(string2), NAME_COLOR)
                             .append(KEY_VALUE_SEPARATOR)
                             .append(" ")
                             .append((new NbtFormatter(string, this.indentationLevel + 1, this.pathParts)).apply(Objects.requireNonNull(nbtElement)));
                this.popPathPart();
                if (iterator.hasNext()) {
                    stringBuilder.append(ENTRY_SEPARATOR).append(string.isEmpty() ? " " : "\n");
                }
            }

            if (!string.isEmpty()) {
                stringBuilder.append("\n").append(Strings.repeat(string, this.indentationLevel));
            }

            stringBuilder.append("}");
            this.result = new RGBColorText(stringBuilder);
            this.popPathPart();
        }
    }

    private void popPathPart() {
        this.pathParts.remove(this.pathParts.size() - 1);
    }

    private void pushPathPart(String part) {
        this.pathParts.add(part);
    }

    protected List<String> getSortedNames(NbtCompound compound) {
        Set<String> set = Sets.newHashSet(compound.getKeys());
        List<String> list = Lists.newArrayList();
        List<String> list2 = ENTRY_ORDER_OVERRIDES.get(this.joinPath());
        if (list2 != null) {

            for (String string : list2) {
                if (set.remove(string)) {
                    list.add(string);
                }
            }

            if (!set.isEmpty()) {
                Stream<String> var10000 = set.stream().sorted();
                Objects.requireNonNull(list);
                var10000.forEach(list::add);
            }
        } else {
            list.addAll(set);
            Collections.sort(list);
        }

        return list;
    }

    public String joinPath() {
        return String.join(".", this.pathParts);
    }

    public void visitEnd(NbtEnd element) {
    }

    public static class RGBColorText {

        public static final RGBEntry NEWLINE = new RGBEntry("\n", 0xFFFFFF);
        final List<RGBEntry> entries = new ArrayList<>();

        public RGBColorText(RGBColorText text) {
            append(text);
        }

        public RGBColorText(String value) {
            append(value, 0xFFFFFF);
        }

        public RGBColorText(Object value, int color) {
            append(String.valueOf(value), color);
        }

        public RGBColorText append(String text, int color) {
            if (text.equals("\n")) {
                entries.add(NEWLINE);
            } else {
                entries.add(new RGBEntry(text, color));
            }
            return this;
        }

        public RGBColorText append(String text) {
            return append(text, 0xFFFFFF);
        }

        public RGBColorText append(RGBColorText c) {
            entries.addAll(c.getEntries());
            return this;
        }

        public List<RGBEntry> getEntries() {
            return entries;
        }

        public record RGBEntry(String value, int color) {

        }
    }
}
