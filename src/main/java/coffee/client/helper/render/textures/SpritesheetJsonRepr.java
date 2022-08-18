/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render.textures;

import lombok.Data;

@Data
class SpritesheetJsonRepr {
    SpritesheetJsonEntry[] frames;

    @Data
    static class SpritesheetJsonEntry {
        String filename;
        Bounds frame;

        @Data
        static class Bounds {
            int x, y;
            int w, h;
        }
    }
}
