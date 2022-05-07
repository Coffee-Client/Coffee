/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.helper;

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
}
