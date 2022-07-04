package coffee.client.helper.manager;

import coffee.client.helper.render.shader.Shader;

public class ShaderManager {
    public static final Shader BLUR = Shader.create("blur", managedShaderEffect -> managedShaderEffect.setUniformValue("radius", 5f));
    public static final Shader FROSTED_GLASS_BLUR = Shader.create("test", managedShaderEffect -> managedShaderEffect.setUniformValue("Range", 8f));
}
