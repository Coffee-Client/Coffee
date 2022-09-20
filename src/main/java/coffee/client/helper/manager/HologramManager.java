/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.manager;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class HologramManager {

    public static Hologram generateDefault(String text, Vec3d pos) {
        return new Hologram().position(pos).text(text).isEgg(false).isSmall(false);
    }

    public static class Hologram {

        String text;
        Vec3d pos;
        boolean isEgg = false, isChild = false, isVisible = false, hasGravity = false, wrapName = true, isMarker = true;

        public Hologram() {
            this.text = "";
            this.pos = Vec3d.ZERO;
        }

        public Hologram text(String text) {
            this.text = text;
            return this;
        }

        public Hologram isMarker(boolean m) {
            this.isMarker = m;
            return this;
        }

        public Hologram hasGravity(boolean hasGravity) {
            this.hasGravity = hasGravity;
            return this;
        }

        public Hologram isVisible(boolean isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Hologram position(Vec3d pos) {
            this.pos = pos;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Hologram wrapsName(boolean wrapName) {
            this.wrapName = wrapName;
            return this;
        }

        public Hologram isEgg(boolean isEgg) {
            this.isEgg = isEgg;
            return this;
        }

        public Hologram isSmall(boolean isChild) {
            this.isChild = isChild;
            return this;
        }

        public ItemStack generate() {
            ItemStack stack = new ItemStack(isEgg ? Items.BEE_SPAWN_EGG : Items.ARMOR_STAND);
            NbtCompound tag = new NbtCompound();
            NbtList pos = new NbtList();
            pos.add(NbtDouble.of(this.pos.x));
            pos.add(NbtDouble.of(this.pos.y));
            pos.add(NbtDouble.of(this.pos.z));
            tag.put("CustomNameVisible", NbtByte.ONE);
            tag.put("CustomName", wrapName ? NbtString.of("{\"text\":\"" + this.text.replaceAll("&", "§") + "\"}") : NbtString.of(this.text.replaceAll("&", "§")));
            tag.put("Invisible", NbtByte.of(!isVisible));
            tag.put("Invulnerable", NbtByte.ONE);
            tag.put("NoGravity", NbtByte.of(!hasGravity));
            tag.put("Small", NbtByte.of(isChild));
            tag.put("Marker", NbtByte.of(isMarker));
            tag.put("Pos", pos);
            if (isEgg) {
                tag.put("id", NbtString.of("minecraft:armor_stand"));
            }
            stack.setSubNbt("EntityTag", tag);
            stack.setCustomName(Text.of("§r§cHologram"));
            return stack;
        }
    }
}
