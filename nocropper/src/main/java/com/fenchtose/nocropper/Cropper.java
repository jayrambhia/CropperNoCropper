package com.fenchtose.nocropper;

import android.graphics.Bitmap;
import android.util.Log;

public class Cropper {

    public final CropInfo cropInfo;
    public final Bitmap originalBitmap;

    private static final String TAG = "Cropper";

    public Cropper(CropInfo cropInfo, Bitmap originalBitmap) {
        this.cropInfo = cropInfo;
        this.originalBitmap = originalBitmap;
    }

    public CropState crop(CropperCallback callback) {
        CropperTask task = new CropperTask(callback);
        task.execute(this);
        return CropState.STARTED;
    }

    public Bitmap cropBitmap() throws IllegalArgumentException {
        Log.i(TAG, "cropinfo: " + cropInfo + ", bitmap: " + originalBitmap.getWidth() + ", " + originalBitmap.getHeight());
        return BitmapUtils.getCroppedBitmap(originalBitmap, cropInfo);
    }
}
