/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.CoffeeMain;
import coffee.client.mixinUtil.SimpleOptionDuck;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.function.Consumer;

@Mixin(SimpleOption.class)
public class SimpleOptionMixin<T> implements SimpleOptionDuck<T> {

    @Shadow
    T value;

    @Shadow
    @Final
    private Consumer<T> changeCallback;

    @Override
    public void setValueDirectly(T o) {
        if (!CoffeeMain.client.isRunning()) {
            this.value = o;
        } else {
            if (!Objects.equals(this.value, o)) {
                this.value = o;
                this.changeCallback.accept(this.value);
            }

        }
    }
}
