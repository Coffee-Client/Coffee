/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.manager;

import coffee.client.helper.render.shader.Shader;

public class ShaderManager {
    public static final Shader BLUR = Shader.create("blur", managedShaderEffect -> managedShaderEffect.setUniformValue("radius", 5f));
    public static final Shader LSD = Shader.create("lsd", managedShaderEffect -> {
    });
}
