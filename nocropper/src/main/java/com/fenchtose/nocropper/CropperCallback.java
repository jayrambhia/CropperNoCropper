package com.fenchtose.nocropper;

import android.graphics.Bitmap;

public abstract class CropperCallback {

    public void onStarted() {

    }

    public abstract void onCropped(Bitmap bitmap);

    public void onOutOfMemoryError() {

    }

    public void onError() {

    }
}
