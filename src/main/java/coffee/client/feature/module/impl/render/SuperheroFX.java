package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.font.renderer.FontRenderer;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import coffee.client.mixin.network.PlayerInteractEntityC2SPacketMixin;
import lombok.AllArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class SuperheroFX extends Module {

    @Setting(name = "Gravity", description = "How much gravity to apply to the particles", min = 0, max = 10, precision = 1)
    double gravity = 2;

    @Setting(name = "Bounce", description = "How much to bounce up when spawned", min = 0, max = 10, precision = 1)
    double bounceUp = 2;

    @Setting(name = "Size random", description = "How much randomness to apply to the size", min = 0, max = 1, precision = 2)
    double sizeRandom = 0.1;

    @Setting(name = "Size", description = "How big the font should be", min = 3, max = 50, precision = 1)
    double size = 1;

    @Setting(name = "Amount", description = "How many fucks to spawn", min = 1, max = 20, precision = 0)
    double amount = 5;

    @Setting(name = "Spread", description = "How much area to give the fuck to spawn", min = 0.1, max = 3, precision = 2)
    double spread = 1;

    @Setting(name = "Lifetime", description = "How long the fucks should stay", min = 100, max = 10000, precision = 0)
    double lifetime = 2000;

    @Setting(name = "Lifetime random", description = "How much randomness to apply to the lifetime", min = 0, max = 3000, precision = 0)
    double lifetimeRandom = 300;

    @Setting(name = "Shadows", description = "Adds shadows to the text")
    boolean shadows = true;

    @Setting(name = "Sync RGB", description = "Makes all the RGB use the same seed")
    boolean syncRgb = false;

    @Setting(name = "Words", description = "Comma seperated list of words to use")
    String words = "Boom, Pow, Wham, Smash, Kapow";

    @Setting(name = "RGB", description = "Whether or not to apply RGB to the fucks")
    boolean rgb = false;

    @Setting(name = "Color", description = "The color of the fucks")
    Color c = new Color(255, 255, 255);

    FontRenderer renderer;
    List<FxEntry> entries = new CopyOnWriteArrayList<>();
    Random r = new Random();

    public SuperheroFX() {
        super("SuperheroFX", "Gaming", ModuleType.RENDER);
    }

    public FontRenderer getRenderer() {
        if (renderer == null) {
            int fsize = 32 * 2;
            try {
                renderer = new FontRenderer(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(CoffeeMain.class.getClassLoader()
                        .getResourceAsStream("Superherofx.ttf"))).deriveFont(Font.PLAIN, fsize), fsize);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return renderer;
    }

    String[] getWords() {
        return Arrays.stream(words.split(",")).map(String::trim).toList().toArray(String[]::new);
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }


    @Override
    public void onFastTick() {
        entries.removeIf(fxEntry -> fxEntry.createdAt + fxEntry.lifetime <= System.currentTimeMillis());
        for (FxEntry entry : entries) {
            entry.addVelocity(0, -gravity / 10000, 0);
            entry.tick();
        }
    }

    @EventListener(type = EventType.PACKET_SEND)
    void packetSend(PacketEvent pe) {
        if (pe.getPacket() instanceof PlayerInteractEntityC2SPacket packet) {
            PlayerInteractEntityC2SPacketMixin mixin = (PlayerInteractEntityC2SPacketMixin) packet;
            int id = mixin.getEntityId();
            Entity e = client.world.getEntityById(id);
            if (e == null) return;
            final boolean[] isAttack = { false };
            packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
                @Override
                public void interact(Hand hand) {

                }

                @Override
                public void interactAt(Hand hand, Vec3d pos) {

                }

                @Override
                public void attack() {
                    isAttack[0] = true;
                }
            });
            if (!isAttack[0]) return;
            Vec3d pos = e.getPos().add(0, e.getHeight() / 2d, 0);
            String[] words = getWords();
            for (int i = 0; i < amount; i++) {
                double randomXOffset = spread * (r.nextDouble() - .5);
                double randomYOffset = spread * (r.nextDouble() - .5);
                double randomZOffset = spread * (r.nextDouble() - .5);
                double velX = (r.nextDouble() - .5) / 60;
                double velZ = (r.nextDouble() - .5) / 60;
                double velY = bounceUp / 100 + (r.nextDouble() - .5) / 60;
                String w = words[r.nextInt(0, words.length)];
                double randomN = r.nextDouble();
                long lifetimeRnd = (long) (randomN * lifetimeRandom);
                FxEntry fe = new FxEntry(w, pos.add(randomXOffset, randomYOffset, randomZOffset), velX, velY, velZ, System.currentTimeMillis(), (long) lifetime + lifetimeRnd, r.nextFloat());
                entries.add(fe);
            }
        }
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        for (FxEntry entry : entries) {
            Vec3d screenSpace = Renderer.R2D.getScreenSpaceCoordinate(entry.pos, matrices);
            if (Renderer.R2D.isOnScreen(screenSpace)) Utils.TickManager.runOnNextRender(() -> {
                Color a = c;
                if (rgb) {
                    if (syncRgb) a = Color.getHSBColor((System.currentTimeMillis() % 2000) / 2000f, 0.7f, 1);
                    else
                        a = Color.getHSBColor((System.currentTimeMillis() % 2000) / 2000f + (float) entry.randomSize, 0.7f, 1);
                }
                long fadeOut = 300;
                long remainingTime = Math.max(0, entry.createdAt + entry.lifetime - System.currentTimeMillis());
                double fadeProgOut = MathHelper.clamp(((double) fadeOut - (double) remainingTime) / (double) fadeOut, 0, 1);
                double fadeProgIn = MathHelper.clamp(((double) fadeOut - (double) (entry.lifetime - remainingTime)) / (double) fadeOut, 0, 1);
                double fadeProg = Transitions.easeOutExpo(Math.max(fadeProgOut, fadeProgIn));
                double scaler = size / 32d;
                MatrixStack st = Renderer.R3D.getEmptyMatrixStack();
                st.translate(screenSpace.x, screenSpace.y, 0);
                st.translate(0, getRenderer().getFontHeight() / 2d, 0);
                st.scale((float) (1 - fadeProg), (float) (1 - fadeProg), 1);

                st.scale((float) scaler, (float) scaler, 1);
                st.scale(1 + (float) ((entry.randomSize - .5) * sizeRandom), 1 + (float) ((entry.randomSize - .5) * sizeRandom), 1);
                if (shadows) {
                    getRenderer().drawCenteredString(st, entry.text, 1f, 1f - getRenderer().getFontHeight() / 2f, .05f, .05f, .05f, 1f);
                }
                getRenderer().drawCenteredString(st, entry.text, 0f, -getRenderer().getFontHeight() / 2f, a.getRed() / 255f, a.getGreen() / 255f, a.getBlue() / 255f, 1f);
            });
        }
    }

    @Override
    public void onHudRender() {

    }

    @AllArgsConstructor
    static class FxEntry {
        String text;
        Vec3d pos;
        double velX, velY, velZ;
        long createdAt;
        long lifetime;
        double randomSize;

        public void addVelocity(double x, double y, double z) {
            velX += x;
            velY += y;
            velZ += z;
        }

        public void tick() {
            pos = pos.add(velX, velY, velZ);
            velX /= 1.01;
            velY /= 1.01;
            velZ /= 1.01;
        }
    }
}
