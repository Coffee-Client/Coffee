/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.notifications;

import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.GameTexture;
import coffee.client.helper.render.Texture;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Notification {
    public final String title;
    public final long creationDate;
    public final Type type;
    public String[] contents;
    public long duration;
    public double posX;
    public double posY;
    public double renderPosX = 0;
    public double renderPosY = 0;
    public double animationProgress = 0;
    public double animationGoal = 0;
    public boolean shouldDoAnimation = false;

    public Notification(long duration, String title, Type type, String... contents) {
        this.duration = duration;
        this.creationDate = System.currentTimeMillis();
        this.contents = contents;
        this.title = title;
        this.type = type;
    }

    /**
     * Creates a new notification rendered on the screen<br> If the duration is below 0, it counts as special. Special codes and their meaning:<br>
     * <ul>
     *  <li>-1: This is an error message (blinks red, doesnt remove itself)</li>
     *  <li>-2: This is a success message (blinks green, doesnt remove itself)</li>
     * </ul><br>
     * Both of these cases make the notification not expire, you have to remove it yourself by setting Notification#duration to 0
     *
     * @param duration How long the notification will stay (special cases are described above
     * @param title    What the title of the notification is (irrelevant when topBar is set)
     * @param topBar   Whether to show this notification at the top of the screen instead of the right
     * @param type     The type of notification
     * @param contents What the contents of the notification is
     * @return The newly created notification
     */
    public static Notification create(long duration, String title, boolean topBar, Type type, String... contents) {
        Notification n = new Notification(duration, title, type, contents);
        n.posY = n.renderPosY = -10;
        if (topBar) {
            NotificationRenderer.topBarNotifications.add(0, n);
        } else {
            NotificationRenderer.notifications.add(0, n);
        }
        return n;
    }

    public static Notification create(long duration, String title, Type type, String... contents) {
        return create(duration, title, false, type, contents);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Notification create(long duration, String title, Type type, String split) {
        List<String> splitContent = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String c : split.split(" +")) {
            if (FontRenderers.getRenderer().getStringWidth(line + " " + c) >= 150) {
                splitContent.add(line.toString());
                line = new StringBuilder();
            }
            line.append(c).append(" ");
        }
        splitContent.add(line.toString());
        return create(duration, title, type, splitContent.toArray(new String[0]));
    }

    public enum Type {
        SUCCESS(GameTexture.NOTIF_SUCCESS.getWhere(), new Color(58, 223, 118)),
        INFO(GameTexture.NOTIF_INFO.getWhere(), new Color(39, 186, 253)),
        WARNING(GameTexture.NOTIF_WARNING.getWhere(), new Color(255, 189, 17)),
        ERROR(GameTexture.NOTIF_ERROR.getWhere(), new Color(254, 92, 92));
        final Color c;
        final Texture i;

        Type(Texture icon, Color color) {
            this.i = icon;
            this.c = color;
        }

        public Texture getI() {
            return i;
        }

        public Color getC() {
            return c;
        }
    }
}
