/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper;

import net.minecraft.server.ServerMetadata;

// For some reason, mixin shits the bed when I define these directly in the mixin, so they have to be moved here..?
public class DirectConnectScreenVariables {
    public static ServerMetadata latestResponse;
    public static String lastIp;

    public static long lastChanged = System.currentTimeMillis();
    public static boolean updated = false;
    public static boolean show = false;
    public static boolean serverTextureKnown = false;
    public static double animationProgress = 0;
}
