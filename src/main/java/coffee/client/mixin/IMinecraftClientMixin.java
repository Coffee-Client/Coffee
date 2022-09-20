/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface IMinecraftClientMixin {

    @Mutable
    @Accessor("session")
    void setSession(Session newSession);

    @Accessor("renderTickCounter")
    RenderTickCounter getRenderTickCounter();

    @Mutable
    @Accessor("profileKeys")
    void setProfileKeys(ProfileKeys keys);

    @Mutable
    @Accessor("userApiService")
    void setUserApiService(UserApiService uas);

    @Accessor("authenticationService")
    YggdrasilAuthenticationService getAuthenticationService();
}
