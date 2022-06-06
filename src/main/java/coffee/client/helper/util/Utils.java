/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.util;

import coffee.client.CoffeeMain;
import coffee.client.helper.Texture;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.mixin.IMinecraftClientMixin;
import coffee.client.mixin.IRenderTickCounterMixin;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

public class Utils {

    public static boolean sendPackets = true;

    public static void throwIfAnyEquals(String message, Object ifEquals, Object... toCheck) {
        for (Object o : toCheck) {
            if (o == ifEquals) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {
        }
    }


    public static void sendPacket(Packet<?> packet) {
        sendPackets = false;
        CoffeeMain.client.player.networkHandler.sendPacket(packet);
        sendPackets = true;
    }

    public static void setClientTps(float tps) {
        IRenderTickCounterMixin accessor = ((IRenderTickCounterMixin) ((IMinecraftClientMixin) CoffeeMain.client).getRenderTickCounter());
        accessor.setTickTime(1000f / tps);
    }

    public static Color getCurrentRGB() {
        return Color.getHSBColor((System.currentTimeMillis() % 4750) / 4750f, 0.5f, 1);
    }

    public static Vec3d getInterpolatedEntityPosition(Entity entity) {
        Vec3d a = entity.getPos();
        Vec3d b = new Vec3d(entity.prevX, entity.prevY, entity.prevZ);
        float p = CoffeeMain.client.getTickDelta();
        return new Vec3d(MathHelper.lerp(p, b.x, a.x), MathHelper.lerp(p, b.y, a.y), MathHelper.lerp(p, b.z, a.z));
    }

    public static void registerBufferedImageTexture(Texture i, BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            byte[] bytes = baos.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            data.flip();
            NativeImageBackedTexture tex = new NativeImageBackedTexture(NativeImage.read(data));
            CoffeeMain.client.execute(() -> CoffeeMain.client.getTextureManager().registerTexture(i, tex));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] splitLinesToWidth(String input, double maxWidth, FontAdapter rendererUsed) {
        List<String> dSplit = List.of(input.split("\n"));
        List<String> splits = new ArrayList<>();
        for (String s : dSplit) {
            List<String> splitContent = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String c : s.split(" ")) {
                if (rendererUsed.getStringWidth(line + c) >= maxWidth - 10) {
                    splitContent.add(line.toString().trim());
                    line = new StringBuilder();
                }
                line.append(c).append(" ");
            }
            splitContent.add(line.toString().trim());
            splits.addAll(splitContent);
        }
        return splits.toArray(new String[0]);
    }

    public static ItemStack generateItemStackWithMeta(String nbt, Item item) {
        try {
            ItemStack stack = new ItemStack(item);
            stack.setNbt(StringNbtReader.parse(nbt));
            return stack;
        } catch (Exception ignored) {
            return new ItemStack(item);
        }
    }

    public static class Inventory {

        public static int slotIndexToId(int index) {
            int translatedSlotId;
            if (index >= 0 && index < 9) {
                translatedSlotId = 36 + index;
            } else {
                translatedSlotId = index;
            }
            return translatedSlotId;
        }

        public static void drop(int index) {
            int translatedSlotId = slotIndexToId(index);
            Objects.requireNonNull(CoffeeMain.client.interactionManager)
                    .clickSlot(Objects.requireNonNull(CoffeeMain.client.player).currentScreenHandler.syncId,
                            translatedSlotId,
                            1,
                            SlotActionType.THROW,
                            CoffeeMain.client.player);
        }

        public static void moveStackToOther(int slotIdFrom, int slotIdTo) {
            Objects.requireNonNull(CoffeeMain.client.interactionManager)
                    .clickSlot(0,
                            slotIdFrom,
                            0,
                            SlotActionType.PICKUP,
                            CoffeeMain.client.player); // pick up item from stack
            CoffeeMain.client.interactionManager.clickSlot(0,
                    slotIdTo,
                    0,
                    SlotActionType.PICKUP,
                    CoffeeMain.client.player); // put item to target
            CoffeeMain.client.interactionManager.clickSlot(0,
                    slotIdFrom,
                    0,
                    SlotActionType.PICKUP,
                    CoffeeMain.client.player); // (in case target slot had item) put item from target back to from
        }
    }

    public static class Math {

        public static double roundToDecimal(double n, int point) {
            if (point == 0) {
                return java.lang.Math.floor(n);
            }
            double factor = java.lang.Math.pow(10, point);
            return java.lang.Math.round(n * factor) / factor;
        }

        public static int tryParseInt(String input, int defaultValue) {
            try {
                return Integer.parseInt(input);
            } catch (Exception ignored) {
                return defaultValue;
            }
        }

        public static Vec3d getRotationVector(float pitch, float yaw) {
            float f = pitch * 0.017453292F;
            float g = -yaw * 0.017453292F;
            float h = MathHelper.cos(g);
            float i = MathHelper.sin(g);
            float j = MathHelper.cos(f);
            float k = MathHelper.sin(f);
            return new Vec3d(i * j, -k, h * j);
        }

        public static boolean isABObstructed(Vec3d a, Vec3d b, World world, Entity requester) {
            RaycastContext rcc = new RaycastContext(a,
                    b,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    requester);
            BlockHitResult bhr = world.raycast(rcc);
            return !bhr.getPos().equals(b);
        }
    }

    public static class Mouse {

        public static double getMouseX() {
            return CoffeeMain.client.mouse.getX() / CoffeeMain.client.getWindow().getScaleFactor();
        }

        public static double getMouseY() {
            return CoffeeMain.client.mouse.getY() / CoffeeMain.client.getWindow().getScaleFactor();
        }
    }

    //---DO NOT REMOVE THIS---
    public static class Packets {

        public static PlayerInteractBlockC2SPacket generatePlace(BlockPos pos) {
            return new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                            Direction.UP,
                            pos,
                            false));
        }

    }

    public static class Players {

        public static boolean isPlayerNameValid(String name) {
            if (name.length() < 3 || name.length() > 16) {
                return false;
            }
            String valid = "abcdefghijklmnopqrstuvwxyz0123456789_";
            boolean isValidEntityName = true;
            for (char c : name.toLowerCase().toCharArray()) {
                if (!valid.contains(c + "")) {
                    isValidEntityName = false;
                    break;
                }
            }
            return isValidEntityName;
        }

        public static int[] decodeUUID(UUID uuid) {
            long sigLeast = uuid.getLeastSignificantBits();
            long sigMost = uuid.getMostSignificantBits();
            return new int[] { (int) (sigMost >> 32), (int) sigMost, (int) (sigLeast >> 32), (int) sigLeast };
        }
    }

    public static class Logging {
        static final Queue<Text> messageQueue = new ArrayDeque<>();

        static void sendMessages() {
            if (CoffeeMain.client.player != null) {
                Text next;
                while ((next = messageQueue.poll()) != null) {
                    CoffeeMain.client.player.sendMessage(next, false);
                }
            }
        }

        public static void warn(String n) {
            message(n, Color.YELLOW);
        }

        public static void success(String n) {
            message(n, new Color(65, 217, 101));
        }

        public static void error(String n) {
            message(n, new Color(214, 93, 62));
        }

        public static void message(String n) {
            message(n, Color.WHITE);
        }

        public static void message(Text text) {
            messageQueue.add(text);
        }

        public static void message(String n, Color c) {
            LiteralText t = new LiteralText(n);
            t.setStyle(t.getStyle().withColor(TextColor.fromRgb(c.getRGB())));
            message(t);
        }

    }

    public static class TickManager {

        static final List<TickEntry> entries = new ArrayList<>();
        static final List<Runnable> nextTickRunners = new ArrayList<>();

        public static void runInNTicks(int n, Runnable toRun) {
            entries.add(new TickEntry(n, toRun));
        }

        public static void tick() {
            Logging.sendMessages();
            for (TickEntry entry : entries.toArray(new TickEntry[0])) {
                entry.v--;
                if (entry.v <= 0) {
                    entry.r.run();
                    entries.remove(entry);
                }
            }
        }

        public static void runOnNextRender(Runnable r) {
            if (CoffeeMain.client.options.hudHidden) {
                return;
            }
            nextTickRunners.add(r);
        }

        public static void render() {
            for (Runnable nextTickRunner : nextTickRunners) {
                nextTickRunner.run();
            }
            nextTickRunners.clear();
        }

        static class TickEntry {

            final Runnable r;
            int v;

            public TickEntry(int v, Runnable r) {
                this.v = v;
                this.r = r;
            }
        }
    }
}
