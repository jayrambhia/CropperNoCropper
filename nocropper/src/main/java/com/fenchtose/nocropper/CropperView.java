package com.fenchtose.nocropper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Jay Rambhia on 11/4/2015.
 */
public class CropperView extends FrameLayout {

    private static final String TAG = "CropperView";
    private CropperImageView mImageView;
    private CropperGridView mGridView;

    private boolean gestureEnabled = true;

    public CropperView(Context context) {
        super(context);
        init(context, null);
    }

    public CropperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CropperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CropperView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    // Make Square
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int orientation = getContext().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT ||
                orientation == Configuration.ORIENTATION_UNDEFINED) {

            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            setMeasuredDimension(width, height);

        } else {

            int height = MeasureSpec.getSize(heightMeasureSpec);
            int width = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            setMeasuredDimension(width, height);

        }

    }

    private void init(Context context, AttributeSet attrs) {
        mImageView = new CropperImageView(context, attrs);
        mGridView = new CropperGridView(context, attrs);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                0);

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.width = 0;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        addView(mImageView, 0, params);
        addView(mGridView, 1, params);

        mImageView.setGestureCallback(new TouchGestureCallback());
    }

    public void release() {
        mImageView.release();
    }

    public void setImageBitmap(Bitmap bm) {
        mImageView.setImageBitmap(bm);
    }

    public void setImageBitmap(Bitmap bm, boolean withPreScaling) {
        mImageView.setImageBitmap(bm, withPreScaling);
    }

    public void setMaxZoom(float zoom) {
        mImageView.setMaxZoom(zoom);
    }

    public Bitmap getCroppedBitmap() throws OutOfMemoryError {
        try {
            return mImageView.cropBitmap();
        } catch (OutOfMemoryError e) {
            throw e;
        }
    }

    public void getCroppedBitmapAsync(final CropperCallback callback) {
        CropperTask task = new CropperTask(callback);
        task.execute(mImageView);
    }

    public RectF getCroppedRect() {
        return mImageView.getCroppedRect();
    }

    public boolean isPreScaling() {
        return mImageView.isPreScaling();
    }

    public float getMaxZoom() {
        return mImageView.getMaxZoom();
    }

    public float getMinZoom() {
        return mImageView.getMinZoom();
    }

    public void setMinZoom(float mMInZoom) {
        mImageView.setMinZoom(mMInZoom);
    }

    public void cropToCenter() {
        mImageView.cropToCenter();
    }

    public void fitToCenter() {
        mImageView.fitToCenter();
    }

    public void setDebug(boolean status) {
        mImageView.setDEBUG(status);
    }

    public int getPaddingColor() {
        return mImageView.getPaddingColor();
    }

    public void setPaddingColor(int paddingColor) {
        mImageView.setPaddingColor(paddingColor);
    }

    public int getCropperWidth() {
        return mImageView != null ? mImageView.getWidth() : 0;
    }

    public boolean isMakeSquare() {
        return mImageView.isMakeSquare();
    }

    public void setMakeSquare(boolean mAddPaddingToMakeSquare) {
        mImageView.setMakeSquare(mAddPaddingToMakeSquare);
    }

    public void replaceBitmap(Bitmap bitmap) {
        mImageView.replaceBitmap(bitmap);
    }

    public boolean isGestureEnabled() {
        return gestureEnabled;
    }

    public void setGestureEnabled(boolean enabled) {
        this.gestureEnabled = enabled;
        mImageView.setGestureEnabled(enabled);
    }

    public void initWithFitToCenter(boolean status) {
        mImageView.setInitWithFitToCenter(status);
    }

    private class TouchGestureCallback implements CropperImageView.GestureCallback {

        @Override
        public void onGestureStarted() {
            mGridView.setShowGrid(true);
        }

        @Override
        public void onGestureCompleted() {
            mGridView.setShowGrid(false);
        }
    }
}
