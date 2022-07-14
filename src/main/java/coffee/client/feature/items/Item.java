/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.items;

import lombok.Getter;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Item {

    @Getter
    final String name, desc;

    public Item(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public Option<?>[] getOptions() {
        try {
            List<Option<?>> o = new ArrayList<>();
            for (Field declaredField : this.getClass().getDeclaredFields()) {
                //                System.out.println(declaredField);
                declaredField.setAccessible(true);
                if (declaredField.get(this) instanceof Option) {
                    o.add((Option<?>) declaredField.get(this));
                }
            }
            return o.toArray(Option[]::new);
        } catch (Exception e) {
            e.printStackTrace();
            return new Option[0];
        }
    }

    public abstract ItemStack generate();
}
