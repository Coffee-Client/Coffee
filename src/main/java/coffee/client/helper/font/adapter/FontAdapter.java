/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.font.adapter;

import net.minecraft.client.util.math.MatrixStack;

public interface FontAdapter {
    void drawString(MatrixStack matrices, String text, float x, float y, int color);

    void drawString(MatrixStack matrices, String text, double x, double y, int color);

    void drawString(MatrixStack matrices, String text, float x, float y, float r, float g, float b, float a);

    void drawCenteredString(MatrixStack matrices, String text, double x, double y, int color);

    void drawCenteredString(MatrixStack matrices, String text, double x, double y, float r, float g, float b, float a);

    float getStringWidth(String text);

    float getFontHeight();

    float getFontHeight(String text);

    float getMarginHeight();

    void drawString(MatrixStack matrices, String s, float x, float y, int color, boolean dropShadow);

    void drawString(MatrixStack matrices, String s, float x, float y, float r, float g, float b, float a, boolean dropShadow);

    String trimStringToWidth(String in, double width);

    String trimStringToWidth(String in, double width, boolean reverse);
}
