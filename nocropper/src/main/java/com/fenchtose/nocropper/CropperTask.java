package com.fenchtose.nocropper;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class CropperTask extends AsyncTask<CropperImageView, Void, Bitmap> {

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
    protected Bitmap doInBackground(CropperImageView... params) {
        try {
            //noinspection WrongThread
            return params[0].cropBitmap();
        } catch (OutOfMemoryError e) {
            isOOMThrown = true;
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap == null && isOOMThrown) {
            callback.onOutOfMemoryError();
            return;
        }

        callback.onCropped(bitmap);
    }

}
