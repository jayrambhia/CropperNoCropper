package com.fenchtose.nocropper;

import android.graphics.Bitmap;

public class BitmapResult {

    private final Bitmap bitmap;
    private final State state;

    private BitmapResult(Bitmap bitmap, State state) {
        this.bitmap = bitmap;
        this.state = state;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public State getState() {
        return state;
    }

    static BitmapResult GestureFailure() {
        return new BitmapResult(null, State.FAILURE_GESTURE_IN_PROCESS);
    }

    static BitmapResult success(Bitmap bitmap) {
        return new BitmapResult(bitmap, State.SUCCESS);
    }

    public enum State {
        STARTED,
        SUCCESS,
        FAILURE_GESTURE_IN_PROCESS
    }
}
