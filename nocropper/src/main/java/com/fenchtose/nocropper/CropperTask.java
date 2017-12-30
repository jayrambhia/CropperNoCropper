package com.fenchtose.nocropper;

import android.os.AsyncTask;

public class CropperTask extends AsyncTask<Cropper, Void, BitmapResult> {

    private final CropperCallback callback;
    private boolean isOOMThrown = false;

    public CropperTask(CropperCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        callback.onStarted();
    }

    @Override
    protected BitmapResult doInBackground(Cropper... params) {
        try {
            //noinspection WrongThread
            return BitmapResult.success(params[0].cropBitmap());
        } catch (OutOfMemoryError e) {
            isOOMThrown = true;
            return null;
        } catch (IllegalArgumentException e) {
            return BitmapResult.error();
        }
    }

    @Override
    protected void onPostExecute(BitmapResult result) {
        if (result == null || isOOMThrown) {
            callback.onOutOfMemoryError();
            return;
        }

        if (result.getState() == CropState.ERROR) {
            callback.onError();
        }

        if (result.getBitmap() != null) {
            callback.onCropped(result.getBitmap());
        }
    }

}
