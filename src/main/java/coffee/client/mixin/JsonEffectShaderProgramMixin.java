/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = JsonEffectShaderProgram.class, priority = 900)
public abstract class JsonEffectShaderProgramMixin {

//    /**
//     * @param arg the string passed to the redirected Identifier constructor
//     * @param id  the actual id passed as an argument to the method
//     *
//     * @return a new Identifier
//     */
//    @Redirect(at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"), method = "loadEffect", require = 0)
//    private static Identifier constructProgramIdentifier(String arg, ResourceManager unused, ShaderStage.Type shaderType, String id) {
//        if (!arg.contains(":")) {
//            return new Identifier(arg);
//        }
//        Identifier split = new Identifier(id);
//        return new Identifier(split.getNamespace(), "shaders/program/" + split.getPath() + shaderType.getFileExtension());
//    }
//
//    /**
//     * Fix identifier creation to allow different namespaces
//     *
//     * <p>Why a redirect ?
//     * <ul>
//     * <li>Because letting the identifier be built will throw an exception, so no ModifyVariable</li>
//     * <li>Because we need to access the original id, so no ModifyArg (alternatively we could use 2 injections and a ThreadLocal but:)</li>
//     * <li>Because we assume other people who may want to do the same change can use this library</li>
//     * </ul>
//     *
//     * @param arg the string passed to the redirected Identifier constructor
//     * @param id  the actual id passed as an argument to the method
//     *
//     * @return a new Identifier
//     */
//    @Redirect(at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;", ordinal = 0), method = "<init>", require = 0)
//    Identifier constructProgramIdentifier(String arg, ResourceManager unused, String id) {
//        if (!id.contains(":")) {
//            return new Identifier(arg);
//        }
//        Identifier split = new Identifier(id);
//        return new Identifier(split.getNamespace(), "shaders/program/" + split.getPath() + ".json");
//    }
}
