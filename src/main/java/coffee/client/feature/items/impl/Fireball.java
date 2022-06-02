/*
 * Copyright (c) Shadow client, Saturn5VFive and contributors 2022. All rights reserved.
 */

package coffee.client.feature.items.impl;

import coffee.client.feature.items.Item;
import coffee.client.feature.items.Option;
import coffee.client.helper.nbt.NbtGroup;
import coffee.client.helper.nbt.NbtList;
import coffee.client.helper.nbt.NbtObject;
import coffee.client.helper.nbt.NbtProperty;
import coffee.client.helper.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Fireball extends Item {
    final Option<Integer> strength = new Option<>("strength", null, Integer.class);

    public Fireball() {
        super("Fireball", "Generates a fireball");
    }

    @Override
    public ItemStack generate() {
        int strength = this.strength.getValue();
        if (strength < 0 || strength > 127) {
            Utils.Logging.error("Strength has to be between 0-127");
            return null;
        }
        ItemStack is = new ItemStack(Items.BLAZE_SPAWN_EGG);
        // "{display:{Name:'{\"text\":\"Fireball\",\"color\":\"dark_gray\",\"italic\":false}',Lore:['{\"text\":\"Fireball of power " + fireballpower + "\",\"color\":\"gray\",\"italic\":false}']},
        // EntityTag:{id:\"minecraft:fireball\",ExplosionPower:" + fireballpower + ",direction:[0.0,-1.0,0.0],power:[0.0,-1.0,0.0]}}"
        String desc;
        if (strength < 10) desc = "baby shit";
        else if (strength < 40) desc = "mid";
        else if (strength < 70) desc = "spicy";
        else if (strength < 100) desc = "monkey destroyer";
        else desc = "classified nuclear weapon";
        NbtGroup ng = new NbtGroup(new NbtObject("EntityTag", new NbtProperty("id", "minecraft:fireball"), new NbtProperty("ExplosionPower", strength)), new NbtObject("display", new NbtProperty("Name", "{\"text\": \"Fireball\", \"color\": \"dark_gray\"}"), new NbtList("Lore", new NbtProperty("{\"text\": \"Fireball of power " + strength + " (" + desc + ")\", \"color\": \"gray\"}"))));
        is.setNbt(ng.toCompound());
        return is;
    }
}
