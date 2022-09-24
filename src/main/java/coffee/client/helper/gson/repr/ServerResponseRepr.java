/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.gson.repr;

import lombok.ToString;
import net.minecraft.text.Text;

@ToString
public class ServerResponseRepr {
    public Text description;
    public String favicon;

    @ToString
    public static class Version {
        public String name;
        public int protocol;
    }

    @ToString
    public static class Players {
        public int max, online;
    }
}
