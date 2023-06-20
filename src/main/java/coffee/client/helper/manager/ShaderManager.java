/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.manager;

import coffee.client.CoffeeMain;
import coffee.client.helper.render.shader.Shader;
import coffee.client.mixinUtil.ShaderEffectDuck;

public class ShaderManager {
    public static final Shader BLUR = Shader.create("blur", managedShaderEffect -> managedShaderEffect.setUniformf("radius", 5f));
    public static final Shader LSD = Shader.create("lsd", managedShaderEffect -> {
    });

    public static final Shader OUTLINE = Shader.create("outline", managedShaderEffect -> {
        ((ShaderEffectDuck) managedShaderEffect.getShader()).coffee_addFakeTarget("bufIn", CoffeeMain.client.worldRenderer.getEntityOutlinesFramebuffer());
        ((ShaderEffectDuck) managedShaderEffect.getShader()).coffee_addFakeTarget("bufOut", CoffeeMain.client.worldRenderer.getEntityOutlinesFramebuffer());
    });
}
