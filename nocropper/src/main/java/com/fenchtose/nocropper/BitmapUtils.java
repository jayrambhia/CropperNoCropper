package com.fenchtose.nocropper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

/**
 * Created by Jay Rambhia on 11/2/2015.
 */
public class BitmapUtils {

    public static Bitmap getCroppedBitmap(Bitmap bitmap, CropInfo cropInfo) throws OutOfMemoryError, IllegalArgumentException {
        if (!cropInfo.addPadding) {
            return Bitmap.createBitmap(bitmap, cropInfo.x, cropInfo.y, cropInfo.width, cropInfo.height);
        }

        return BitmapUtils.addPadding(bitmap, cropInfo, cropInfo.paddingColor);
    }

    public static Bitmap addPadding(Bitmap bmp, int color) throws OutOfMemoryError {

        if (bmp == null) {
            return null;
        }

        Bitmap bitmap = null;

        try {
            int biggerParam = Math.max(bmp.getWidth(), bmp.getHeight());
            bitmap = Bitmap.createBitmap(biggerParam, biggerParam, bmp.getConfig());
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(color);

            int top = bmp.getHeight() > bmp.getWidth() ? 0 : (bmp.getWidth() - bmp.getHeight()) / 2;
            int left = bmp.getWidth() > bmp.getHeight() ? 0 : (bmp.getHeight() - bmp.getWidth()) / 2;

            canvas.drawBitmap(bmp, left, top, null);
            return bitmap;
        } catch (OutOfMemoryError e) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }

            throw e;
        }
    }

    public static Bitmap addPadding(Bitmap bmp, CropInfo info, int color) throws OutOfMemoryError {

        if (bmp == null) {
            return null;
        }

        Bitmap bitmap = null;

        try {

            int biggerParam = Math.max(info.width + 2*info.horizontalPadding, info.height + 2*info.verticalPadding);
            bitmap = Bitmap.createBitmap(biggerParam, biggerParam, bmp.getConfig());
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(color);

            Rect dest = new Rect(info.horizontalPadding, info.verticalPadding, info.horizontalPadding + info.width, info.verticalPadding + info.height);
            Rect src = new Rect(info.x, info.y, info.x + info.width, info.y + info.height);

            Log.d("BitmapUtils", "src: " + src);
            Log.d("BitmapUtils", "dest: " + dest);

            canvas.drawBitmap(bmp, src, dest, null);
            return bitmap;

        } catch (OutOfMemoryError e) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }

            throw e;
        }
    }

}
