/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.gson.GsonSupplier;
import coffee.client.helper.render.Texture;
import coffee.client.helper.util.Utils;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Taco extends Command {
    public static final File storage = new File(CoffeeMain.BASE, "taco.sip");
    public static final List<Frame> frames = new ArrayList<>();
    public static final AtomicBoolean init = new AtomicBoolean(false);
    static final File gifPath = new File(CoffeeMain.BASE, "tacoFrames");
    public static TacoConfig config = new TacoConfig();
    public static long currentFrame = 0;
    static final Thread ticker = new Thread(() -> {
        while (true) {
            long sleepTime = 1000 / config.fps;
            currentFrame++;
            if (currentFrame >= frames.size()) {
                currentFrame = 0;
            }
            Utils.sleep(sleepTime);
        }
    });

    public Taco() {
        super("Taco", "Config for the taco hud", "taco");
        Events.registerEventHandler(EventType.CONFIG_SAVE, event -> saveConfig(), 0);
        Events.registerEventHandler(EventType.POST_INIT, event -> { // we in game, context is made, we can make textures
            if (!init.get()) {
                initFramesAndConfig();
            }
            init.set(true);
        }, 0);
    }

    static void initFramesAndConfig() {
        if (init.get()) {
            throw new IllegalStateException();
        }
        try {
            ticker.start();
        } catch (Exception ignored) {

        }
        if (!gifPath.exists()) {
            //noinspection ResultOfMethodCallIgnored
            gifPath.mkdir();
        }
        try {
            if (!storage.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                storage.delete();
            }
            if (!storage.exists()) {
                //noinspection ResultOfMethodCallIgnored
                storage.createNewFile();
                CoffeeMain.log(Level.INFO, "Skipping taco config file because it doesnt exist");
                return;
            }
            String a = FileUtils.readFileToString(storage, StandardCharsets.UTF_8);
            Gson gson = GsonSupplier.getGson();
            config = gson.fromJson(a, TacoConfig.class);
            if (config == null) {
                config = new TacoConfig();
            }
            initFrames();
        } catch (Exception e) {
            CoffeeMain.log(Level.ERROR, "Failed to read taco config");
            e.printStackTrace();
            if (storage.exists()) {
                //noinspection ResultOfMethodCallIgnored
                storage.delete();
            }
        }
    }

    static void initFrames() {
        checkGifPath();
        for (Frame frame : frames) {
            CoffeeMain.client.getTextureManager().destroyTexture(frame.getI());
        }
        frames.clear();
        Frame.frameCounter = 0;
        File[] a = Objects.requireNonNull(gifPath.listFiles()).clone();
        List<String> framesSorted = Arrays.stream(a).map(File::getName).sorted().toList();
        for (String file : framesSorted) {
            if (!file.endsWith(".gif")) {
                continue;
            }
            File f = Arrays.stream(a).filter(file1 -> file1.getName().equals(file)).findFirst().orElseThrow();
            try {
                ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                ImageInputStream stream = ImageIO.createImageInputStream(f);
                reader.setInput(stream);

                int count = reader.getNumImages(true);
                for (int index = 0; index < count; index++) {
                    BufferedImage frame = reader.read(index);
                    Frame frame1 = new Frame(frame);
                    frames.add(frame1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Frame getCurrentFrame() {
        if (currentFrame >= frames.size()) {
            currentFrame = 0;
        }
        if (frames.isEmpty()) {
            return null;
        }
        return frames.get((int) currentFrame);
    }

    static void saveConfig() {
        Gson gson = GsonSupplier.getGson();
        String json = gson.toJson(config);
        try {
            FileUtils.writeStringToFile(storage, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            CoffeeMain.log(Level.ERROR, "Failed to write taco config");
            e.printStackTrace();
        }
    }

    static void copyGifFiles(File f) {
        for (File file : Objects.requireNonNull(gifPath.listFiles())) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        try {
            FileUtils.copyFile(f, new File(gifPath, f.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void checkGifPath() {
        if (!gifPath.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            gifPath.delete();
        }
        if (!gifPath.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                gifPath.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("fps 30", "play /path/to/gif/file.gif", "toggle");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        if (index == 0) {
            return new PossibleArgument(ArgumentType.STRING, "fps", "play", "toggle");
        }
        String a = args[0];
        if (index == 1) {
            return switch (a.toLowerCase()) {
                case "fps" -> new PossibleArgument(ArgumentType.NUMBER, "<new fps>");
                case "play" -> new PossibleArgument(ArgumentType.STRING, "<path to gif file>");
                default -> super.getSuggestionsWithType(index, args);
            };
        }
        return super.getSuggestionsWithType(index, args);
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide property");
        switch (args[0].toLowerCase()) {
            case "fps" -> {
                validateArgumentsLength(args, 2, "Provide new FPS");
                int i = Utils.Math.tryParseInt(args[1], -1);
                if (i < 1 || i > 1000) {
                    throw new CommandException("Fps outside of bounds");
                }
                config.fps = i;
                success("set fps to " + i);
            }
            case "play" -> {
                validateArgumentsLength(args, 2, "Provide file name");
                File f = new File(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (!f.exists()) {
                    throw new CommandException("File doesn't exist");
                }
                if (!f.isFile()) {
                    throw new CommandException("File is not a file");
                }
                message("Loading gif frames");
                checkGifPath();
                message("Copying frames");
                copyGifFiles(f);
                try {
                    message("Initializing frames");
                    initFrames();
                    success("Initialized frames!");
                } catch (Exception e) {
                    error("Failed to init: " + e.getMessage());
                    error("Logs have more detail");
                    e.printStackTrace();
                }
            }
            case "toggle" -> {
                config.enabled = !config.enabled;
                if (config.enabled) {
                    success("Taco is now tacoing");
                } else {
                    message("Taco is no longer tacoing :(");
                }
            }
        }
    }

    public static class TacoConfig {
        public long fps = 30;
        public boolean enabled = false;
    }

    public static class Frame {
        static long frameCounter = 0;
        final Texture i;

        public Frame(BufferedImage image) {
            i = new Texture("taco/frame_" + frameCounter);
            frameCounter++;
            Utils.registerBufferedImageTexture(i, image);
        }

        public Texture getI() {
            return i;
        }
    }
}
