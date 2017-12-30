package com.fenchtose.nocropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class ScaledCropper {

    public final Context context;
    public final CropInfo cropInfo;
    public final Bitmap originalBitmap;
    public final float scale;

    public ScaledCropper(Context context, CropInfo cropInfo, Bitmap originalBitmap, float scale) {
        this.context = context;
        this.cropInfo = cropInfo;
        this.originalBitmap = originalBitmap;
        this.scale = scale;
    }

    public CropState crop(CropperCallback callback) {
        ScaledCropperTask task = new ScaledCropperTask(callback);
        task.execute(this);
        return CropState.STARTED;
    }

    public Bitmap cropBitmap() throws IllegalArgumentException {
        CropInfo scaledInfo = cropInfo.scaleInfo(scale);
        Log.i("ScaledCropper", "scaled crop info: " + scaledInfo);
        return BitmapUtils.getCroppedBitmap(originalBitmap, scaledInfo);
    }
}
