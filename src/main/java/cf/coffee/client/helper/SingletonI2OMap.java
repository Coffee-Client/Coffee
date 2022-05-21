package cf.coffee.client.helper;

import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;

public class SingletonI2OMap<T> implements Int2ObjectMap<T> {
    T val;
    int size;

    public SingletonI2OMap(T value, int size) {
        this.val = value;
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return val.equals(value);
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends T> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(T t) {

    }

    @Override
    public T defaultReturnValue() {
        return val;
    }

    @Override
    public ObjectSet<Entry<T>> int2ObjectEntrySet() {
        ObjectSet<Entry<T>> set = new ObjectArraySet<>();
        for (int i = 0; i < size; i++) {
            set.add(new AbstractInt2ObjectMap.BasicEntry<>(i, val));
        }
        return set;
    }

    @Override
    public IntSet keySet() {
        IntSet is = new IntArraySet();
        for (int i = 0; i < size; i++) {
            is.add(i);
        }
        return is;
    }

    @Override
    public ObjectCollection<T> values() {
        ObjectCollection<T> col = new ObjectArraySet<>();
        for (int i = 0; i < size; i++) {
            col.add(val);
        }
        return col;
    }

    @Override
    public T get(int i) {
        if (i < size) return val;
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean containsKey(int i) {
        return i < size;
    }

    @Override
    public void forEach(BiConsumer<? super Integer, ? super T> consumer) {
        for (int i = 0; i < size; i++) {
            consumer.accept(i, val);
        }
    }
}
