package coffee.client.feature.items.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.items.Item;
import coffee.client.helper.nbt.NbtGroup;
import coffee.client.helper.nbt.NbtList;
import coffee.client.helper.nbt.NbtObject;
import coffee.client.helper.nbt.NbtProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

public class InfiniteEntity extends Item {

    public InfiniteEntity() {
        super("InfiniteEntity", "Creates an entity that will corrupt the chunk it was generated in");
    }

    @Override
    public ItemStack generate() {
        Vec3d pos = CoffeeMain.client.player.getPos();
        NbtGroup ng = new NbtGroup(new NbtObject("EntityTag",
                new NbtList("Pos", new NbtProperty(pos.x), new NbtProperty(Double.MAX_VALUE), new NbtProperty(pos.z))));
        NbtCompound nc = ng.toCompound();
        ItemStack is = new ItemStack(Items.COW_SPAWN_EGG);
        is.setNbt(nc);
        return is;
    }
}
