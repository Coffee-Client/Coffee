/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.font;

import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.font.adapter.impl.QuickFontAdapter;
import coffee.client.helper.font.renderer.FontRenderer;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FontRenderers {
    private static final List<QuickFontAdapter> fontRenderers = new ArrayList<>();
    private static FontAdapter normal;
    private static FontAdapter mono;

    public static FontAdapter getRenderer() {
        return normal;
    }

    public static void setRenderer(FontAdapter normal) {
        FontRenderers.normal = normal;
    }

    public static FontAdapter getMono() {
        if (mono == null) {
            int fsize = 18 * 2;
            try {
                mono = (new QuickFontAdapter(new FontRenderer(Font.createFont(Font.TRUETYPE_FONT,
                                Objects.requireNonNull(FontRenderers.class.getClassLoader().getResourceAsStream("Mono.ttf")))
                        .deriveFont(Font.PLAIN, fsize), fsize)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return mono;
    }

    public static QuickFontAdapter getCustomSize(int size) {
        int size1 = size;
        size1 *= 2;
        for (QuickFontAdapter fontRenderer : fontRenderers) {
            if (fontRenderer.getSize() == size1) {
                return fontRenderer;
            }
        }
        int fsize = size1;
        try {
            QuickFontAdapter bruhAdapter = (new QuickFontAdapter(new FontRenderer(Font.createFont(Font.TRUETYPE_FONT,
                            Objects.requireNonNull(FontRenderers.class.getClassLoader().getResourceAsStream("Font.ttf")))
                    .deriveFont(Font.PLAIN, fsize), fsize)));
            fontRenderers.add(bruhAdapter);
            return bruhAdapter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
