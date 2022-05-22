/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import net.minecraft.client.util.GlfwUtil;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

public class RageQuit extends Command {

    public RageQuit() {
        super("RageQuit", "U mad?", "ragequit");
    }

    public static boolean shutdown(int time) throws IOException {
        String shutdownCommand, t = time == 0 ? "now" : String.valueOf(time);

        if (SystemUtils.IS_OS_AIX) {
            shutdownCommand = "shutdown -Fh " + t;
        } else if (SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD || SystemUtils.IS_OS_UNIX) {
            shutdownCommand = "shutdown -h " + t;
        } else if (SystemUtils.IS_OS_HP_UX) {
            shutdownCommand = "shutdown -hy " + t;
        } else if (SystemUtils.IS_OS_IRIX) {
            shutdownCommand = "shutdown -y -g " + t;
        } else if (SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_SUN_OS) {
            shutdownCommand = "shutdown -y -i5 -g" + t;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            shutdownCommand = "shutdown.exe /s /t " + t;
        } else {
            return false;
        }

        Runtime.getRuntime().exec(shutdownCommand);
        return true;
    }

    @Override
    public void onExecute(String[] args) {
        try {
            boolean i = shutdown(0);
            if (!i) {
                throw new Exception();
            }
        } catch (Exception ignored) {
            GlfwUtil.makeJvmCrash();
        }
    }
}
