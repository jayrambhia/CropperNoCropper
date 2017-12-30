package com.fenchtose.nocropper;

import android.os.AsyncTask;

public class ScaledCropperTask extends AsyncTask<ScaledCropper, Void, BitmapResult> {

    private final CropperCallback callback;
    private boolean isOOMThrown = false;

    public ScaledCropperTask(CropperCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        callback.onStarted();
    }

    @Override
    protected BitmapResult doInBackground(ScaledCropper... params) {
        try {
            //noinspection WrongThread
            return BitmapResult.success(params[0].cropBitmap());
        } catch (OutOfMemoryError e) {
            isOOMThrown = true;
            return BitmapResult.error();
        } catch (IllegalArgumentException e) {
            return BitmapResult.error();
        }
    }

    @Override
    protected void onPostExecute(BitmapResult result) {
        if (result.getState() == CropState.ERROR) {
            if (isOOMThrown) {
                callback.onOutOfMemoryError();
                return;
            }

            callback.onError();
            return;
        }


        callback.onCropped(result.getBitmap());
    }

}
