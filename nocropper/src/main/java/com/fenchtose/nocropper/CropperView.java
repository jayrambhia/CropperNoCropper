package com.fenchtose.nocropper;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Jay Rambhia on 11/4/2015.
 */
public class CropperView extends FrameLayout {

    private static final String TAG = "CropperView";
    private CropperImageView mImageView;
    private CropperGridView mGridView;

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
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    private void init(Context context, AttributeSet attrs) {
        mImageView = new CropperImageView(context, attrs);
        mGridView = new CropperGridView(context, attrs);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                0);
        addView(mImageView, 0, params);
        addView(mGridView, 1, params);

        mImageView.setGestureCallback(new TouchGestureCallback());
    }

    public void setImageBitmap(Bitmap bm) {
        mImageView.setImageBitmap(bm);
    }

    public void setMaxZoom(float zoom) {
        mImageView.setMaxZoom(zoom);
    }

    public Bitmap getCroppedBitmap() {
        return mImageView.getCroppedBitmap();
    }

    public boolean isPreScaling() {
        return mImageView.isDoPreScaling();
    }

    public void setPreScaling(boolean doPreScaling) {
        mImageView.setDoPreScaling(doPreScaling);
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

    public void cropToCenter() {
        mImageView.cropToCenter();
    }

    public void fitToCenter() {
        mImageView.fitToCenter();
    }

    public void setPaddingColor(int color) {
        mImageView.setPaddingColor(color);
    }

    public int getPaddingColor() {
        return mImageView.getPaddingColor();
    }
}
