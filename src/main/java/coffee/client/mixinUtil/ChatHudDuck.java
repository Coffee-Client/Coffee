/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixinUtil;

import net.minecraft.text.Text;

public interface ChatHudDuck {
    int coffee_addChatMessage(Text content);

    void coffee_removeChatMessage(int id);
}
