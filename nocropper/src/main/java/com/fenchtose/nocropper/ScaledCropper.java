package com.fenchtose.nocropper;

import android.graphics.Bitmap;

public class ScaledCropper extends Cropper {

    public ScaledCropper(CropInfo cropInfo, Bitmap originalBitmap, float scale) {
        super(cropInfo.scaleInfo(scale), originalBitmap);
    }

    public CropState crop(CropperCallback callback) {
        CropperTask task = new CropperTask(callback);
        task.execute(this);
        return CropState.STARTED;
    }
}
