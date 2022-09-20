/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.StreamlineArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.helper.nbt.NbtElement;
import coffee.client.helper.nbt.NbtGroup;
import coffee.client.helper.nbt.NbtList;
import coffee.client.helper.nbt.NbtObject;
import coffee.client.helper.nbt.NbtProperty;
import coffee.client.helper.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class HoloImage extends Command {

    static final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(2)).build();
    ExecutorService backBurner = Executors.newFixedThreadPool(1);
    AtomicBoolean running = new AtomicBoolean(false);

    public HoloImage() {
        super("HoloImage", "Creates armor stands that build a certain image", "holoimage");
    }

    static void run(StreamlineArgumentParser argParser) throws CommandException {
        String url = argParser.consumeString();
        message("Downloading image...");
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "coffee/1.0").build();
        try {
            HttpResponse<byte[]> send = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            byte[] body = send.body();
            //            System.out.println(Arrays.toString(body));
            //            System.out.println(new String(body).replaceAll("\\P{Print}", "."));
            BufferedImage read = ImageIO.read(new ByteArrayInputStream(body));
            success("Downloaded image, resizing");
            int newWidth = argParser.consumeInt(read.getWidth());
            double ratio = (double) newWidth / read.getWidth();
            int newHeight = (int) (read.getHeight() * ratio);
            BufferedImage n = resize(read, newWidth, newHeight);

            List<MutableText> names = new ArrayList<>();
            float yOffset = n.getHeight() / 4.5f;
            for (int y = 0; y < n.getHeight(); y++) {
                MutableText currentName = Text.literal("");
                for (int x = 0; x < n.getWidth(); x++) {
                    int rgb = n.getRGB(x, y);
                    Color c = new Color(rgb);
                    int t = c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
                    currentName.append(Text.literal("█").styled(style -> style.withColor(t)));
                }
                names.add(currentName);
            }
            List<List<MutableText>> partition = Lists.partition(names, 9 * 3);
            List<NbtObject> shulkers = new ArrayList<>();
            int total = 0;
            Vec3d pos = CoffeeMain.client.player.getPos();
            for (List<MutableText> mutableTexts : partition) {
                List<NbtObject> stacks = new ArrayList<>();
                for (MutableText mutableText : mutableTexts) {
                    stacks.add(new NbtObject("EntityTag",
                        new NbtProperty("id", "minecraft:armor_stand"),
                        new NbtProperty("NoGravity", true),
                        new NbtProperty("Invisible", true),
                        new NbtProperty("Marker", true),
                        new NbtList("Pos", new NbtProperty(pos.x), new NbtProperty(pos.y + yOffset), new NbtProperty(pos.z)),
                        new NbtProperty("CustomNameVisible", true),
                        new NbtProperty("CustomName", Text.Serializer.toJson(mutableText))));
                    total++;
                    yOffset -= 1 / 4.5f;
                }
                shulkers.add(new NbtObject("BlockEntityTag",
                    new NbtList("Items",
                        IntStream.range(0, stacks.size())
                            .mapToObj(value -> new NbtObject("",
                                new NbtProperty("Slot", (byte) value),
                                new NbtProperty("id", Registry.ITEM.getId(Items.BAT_SPAWN_EGG).toString()),
                                new NbtProperty("Count", (byte) 1),
                                new NbtObject("tag", stacks.get(value))))
                            .toArray(NbtElement[]::new))));
            }
            ItemStack chest = new ItemStack(Items.CHEST);
            NbtGroup ng = new NbtGroup(new NbtObject("BlockEntityTag",
                new NbtList("Items",
                    IntStream.range(0, shulkers.size())
                        .mapToObj(value -> new NbtObject("",
                            new NbtProperty("Slot", (byte) value),
                            new NbtProperty("id", Registry.ITEM.getId(Items.SHULKER_BOX).toString()),
                            new NbtProperty("Count", (byte) 1),
                            new NbtObject("tag", shulkers.get(value))))
                        .toArray(NbtElement[]::new))));
            chest.setNbt(ng.toCompound());
            CoffeeMain.client.interactionManager.clickCreativeStack(chest, Utils.Inventory.slotIndexToId(CoffeeMain.client.player.getInventory().selectedSlot));
            success("Gave you a chest with all the spawn eggs");
            message(String.format("Total spawn eggs: %s. Total shulkers: %s", total, shulkers.size()));
        } catch (IOException | InterruptedException e) {
            error("Failed to download the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
            new PossibleArgument(ArgumentType.STRING, "<url>"),
            new PossibleArgument(ArgumentType.NUMBER, "<image width>"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        if (running.get()) {
            error("There's already a hologram job running, wait a second");
            return;
        }
        validateArgumentsLength(args, 1, "Image URL Required");
        StreamlineArgumentParser argParser = new StreamlineArgumentParser(args);
        running.set(true);
        backBurner.execute(() -> {
            try {
                run(argParser);
            } catch (Exception e) {
                error("Something went wrong: " + e.getMessage());
                message("The logs contain the entire error, please send your latest.log file to the developer.");
                e.printStackTrace();
            } finally {
                running.set(false);
            }
        });
    }
}
