package com.fenchtose.nocropper;

import android.graphics.Bitmap;

public class BitmapResult {

    private final Bitmap bitmap;
    private final CropState state;

    private BitmapResult(Bitmap bitmap, CropState state) {
        this.bitmap = bitmap;
        this.state = state;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public CropState getState() {
        return state;
    }

    static BitmapResult GestureFailure() {
        return new BitmapResult(null, CropState.FAILURE_GESTURE_IN_PROCESS);
    }

    static BitmapResult success(Bitmap bitmap) {
        return new BitmapResult(bitmap, CropState.SUCCESS);
    }

    static BitmapResult error() {
        return new BitmapResult(null, CropState.ERROR);
    }

}
