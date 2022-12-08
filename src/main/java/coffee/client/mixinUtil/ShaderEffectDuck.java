/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixinUtil;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;

import java.util.List;

public interface ShaderEffectDuck {
    void addFakeTarget(String name, Framebuffer buffer);
    List<PostEffectPass> getPasses();
}
