/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.notifications.hudNotif;

import coffee.client.CoffeeMain;
import coffee.client.helper.render.MSAAFramebuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HudNotificationRenderer {
    public static final HudNotificationRenderer instance = new HudNotificationRenderer();
    final List<HudNotification> notifs = new CopyOnWriteArrayList<>();

    void addNotification(HudNotification notif) {
        this.notifs.add(notif);
    }

    public void render(MatrixStack stack) {
        notifs.removeIf(HudNotification::isDead);
        double x = CoffeeMain.client.getWindow().getScaledWidth() - 5;
        final double[] y = { 5 };
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> {
            for (HudNotification notif : notifs) {
                notif.render(stack, x, y[0]);

                double moveAnim = MathHelper.clamp(notif.easeInOutBack(MathHelper.clamp(notif.getAnimProg(), 0, 0.5) * 2), 0, 1);
                y[0] += (notif.getHeight() + 5) * moveAnim;
            }
        });
    }
}
