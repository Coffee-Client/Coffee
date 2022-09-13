/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.mixin;

import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Session.class)
public interface ISessionMixin {
    @Mutable
    @Accessor("username")
    void setUsername(String username);

    @Mutable
    @Accessor("uuid")
    void setUuid(String uuid);

    @Mutable
    @Accessor("accessToken")
    void setAccessToken(String accessToken);

}
