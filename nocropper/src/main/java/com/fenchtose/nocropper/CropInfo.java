package com.fenchtose.nocropper;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

public class CropInfo {

    private final float xTrans;
    private final float yTrans;
    private final float scale;
    private final RectF baseRect;
    private final int[] bitmapSize;

    public CropInfo(Matrix matrix, int viewSize, Bitmap bitmap) {

        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);

        xTrans = matrixValues[Matrix.MTRANS_X];
        yTrans = matrixValues[Matrix.MTRANS_Y];
        scale = matrixValues[Matrix.MSCALE_X];

        baseRect = new RectF();
        baseRect.left = -xTrans/scale;
        baseRect.top = -yTrans/scale;
        baseRect.right = baseRect.left + viewSize/scale;
        baseRect.bottom = baseRect.top + viewSize/scale;

        bitmapSize = new int[2];
        bitmapSize[0] = bitmap.getWidth();
        bitmapSize[1] = bitmap.getHeight();
    }

    public float getxTrans() {
        return xTrans;
    }

    public float getyTrans() {
        return yTrans;
    }

    public float getScale() {
        return scale;
    }

    public RectF getBaseRect() {
        return baseRect;
    }

    public int[] getBitmapSize() {
        return bitmapSize;
    }
}
