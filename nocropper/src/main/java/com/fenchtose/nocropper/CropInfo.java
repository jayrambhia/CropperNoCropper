package com.fenchtose.nocropper;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class CropInfo {
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final float scale;
    public final boolean addPadding;
    public final int verticalPadding;
    public final int horizontalPadding;
    public final int paddingColor;

    public CropInfo(int x, int y, int width, int height, float scale) {
        this(x, y, width, height, scale, false, 0, 0, -1);
    }

    public CropInfo(int x, int y, int width, int height, float scale, boolean addPadding, int horizontalPadding, int verticalPadding, int paddingColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.addPadding = addPadding;
        this.horizontalPadding = horizontalPadding;
        this.verticalPadding = verticalPadding;
        this.paddingColor = paddingColor;
    }

    public static CropInfo cropCompleteBitmap(Bitmap bitmap, float scale, boolean paddingToMakeSquare, int horizontalPadding, int verticalPadding, int paddingColor) {
        return new CropInfo(0, 0, bitmap.getWidth(), bitmap.getHeight(), scale, paddingToMakeSquare, horizontalPadding, verticalPadding, paddingColor);
    }

    public static CropInfo cropFromRect(Rect rect, float scale, boolean paddingToMakeSquare, int horizontalPadding, int verticalPadding, int paddingColor) {
        return new CropInfo(rect.left, rect.top, rect.width(), rect.height(), scale, paddingToMakeSquare, horizontalPadding, verticalPadding, paddingColor);
    }

    public CropInfo scaleInfo(float scale) {
        return new CropInfo(
                (int)(x * scale),
                (int)(y * scale),
                (int)(width * scale),
                (int)(height * scale),
                scale,
                addPadding,
                (int)(horizontalPadding * scale),
                (int)(verticalPadding * scale),
                paddingColor);
    }

    public CropInfo rotateInfo(int bitmapWidth, int bitmapHeight) {
        return new CropInfo(
                bitmapHeight - y - height,
                x,
                height,
                width,
                scale,
                addPadding,
                verticalPadding,
                horizontalPadding,
                paddingColor
        );
    }

    @Override
    public String toString() {
        return "CropInfo{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", scale=" + scale +
                ", addPadding=" + addPadding +
                ", verticalPadding=" + verticalPadding +
                ", horizontalPadding=" + horizontalPadding +
                '}';
    }
}
