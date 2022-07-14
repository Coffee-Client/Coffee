/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Identifier;

public class Texture extends Identifier {
    public Texture(String path) {
        super("coffee", validatePath(path));
    }

    public Texture(Identifier i) {
        super(i.getNamespace(), i.getPath());
    }

    static String validatePath(String path) {
        if (isValid(path)) {
            return path;
        }
        StringBuilder ret = new StringBuilder();
        for (char c : path.toLowerCase().toCharArray()) {
            if (isPathCharacterValid(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public void bind() {
        RenderSystem.setShaderTexture(0, this);
    }
}
