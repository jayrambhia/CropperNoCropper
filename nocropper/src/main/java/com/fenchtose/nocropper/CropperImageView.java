package com.fenchtose.nocropper;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * Created by Jay Rambhia on 10/29/2015.
 */
public class CropperImageView extends ImageView {

    private static final String TAG = "CropperImageView";

    private float[] mMatrixValues = new float[9];

    protected GestureDetector mGestureDetector;

    protected ScaleGestureDetector mScaleDetector;

    private float mMinZoom = 0;
    private float mMaxZoom = 0;
    private float mBaseZoom = 0;
    private float mBaseZoomBigger = 0;

    private float mFocusX;
    private float mFocusY;

    private boolean isMaxZoomSet = false;
    private boolean isMinZoomSetByUser = false;
    private boolean mFirstRender = true;

    private Bitmap mBitmap;
    private boolean doPreScaling = false;
    private float mPreScale;

    private boolean mAddPaddingToMakeSquare = true;

    private GestureCallback mGestureCallback;

    private boolean showAnimation = true;
    private boolean isAdjusting = false;
    private boolean isCropping = false;

    private int mPaintColor = Color.WHITE;

    public boolean DEBUG = false;

    private boolean gestureEnabled = true;

    private boolean initWithFitToCenter = false;

    public CropperImageView(Context context) {
        super(context);
        init(context, null);
    }

    public CropperImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CropperImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CropperImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.nocropper__CropperView);
            if (mTypedArray != null) {
                mPaintColor = mTypedArray.getColor(R.styleable.nocropper__CropperView_nocropper__padding_color, mPaintColor);
                mAddPaddingToMakeSquare = mTypedArray.getBoolean(R.styleable.nocropper__CropperView_nocropper__add_padding_to_make_square, true);
                initWithFitToCenter = mTypedArray.getBoolean(R.styleable.nocropper__CropperView_nocropper__fit_to_center, false);
                mTypedArray.recycle();
            }
        }

        GestureListener mGestureListener = new GestureListener();
        mGestureDetector = new GestureDetector(context, mGestureListener, null, true);

        ScaleListener mScaleListener = new ScaleListener();
        mScaleDetector = new ScaleGestureDetector(context, mScaleListener);

        setScaleType(ScaleType.MATRIX);
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (DEBUG) {
            Log.i(TAG, "onLayout: " + changed + " [" + left + ", " + top + ", " + right + ", " + bottom + "]");
        }

        if (changed || mFirstRender) {

            if (mFirstRender) {
                final Drawable drawable = getDrawable();
                if (drawable == null) {
                    if (DEBUG) {
                        Log.e(TAG, "drawable is null");
                    }
                    return;
                }

                int orientation = getResources().getConfiguration().orientation;

                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mBaseZoom = (float) (right - left) / Math.max(drawable.getIntrinsicHeight(),
                            drawable.getIntrinsicWidth());
                    mBaseZoomBigger = (float) (right - left) / Math.min(drawable.getIntrinsicHeight(),
                            drawable.getIntrinsicWidth());
                } else {
                    mBaseZoom = (float) (bottom - top) / Math.max(drawable.getIntrinsicHeight(),
                            drawable.getIntrinsicWidth());

                    mBaseZoomBigger = (float) (bottom - top) / Math.min(drawable.getIntrinsicHeight(),
                            drawable.getIntrinsicWidth());
                }

                if (isMaxZoomSet && mBaseZoom > mMaxZoom) {
                    // base zoom should not be greater than max zoom.
                    mBaseZoom = mMaxZoom;
                    mBaseZoomBigger = mMaxZoom;
                    if (mMinZoom > mMaxZoom) {
                        Log.e(TAG, "min zoom is greater than max zoom. Changing min zoom = max zoom");
                        _setMinZoom(mMaxZoom);
                    }
                }

                if (mMinZoom <= 0 || !isMinZoomSetByUser) {
                    _setMinZoom(mBaseZoom);
                }

                if (initWithFitToCenter) {
                    fitToCenter(drawable, bottom - top);
                } else {
                    cropToCenter(drawable, bottom - top);
                }

                mFirstRender = false;

            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isAdjusting) {
            return true;
        }

        if (isCropping) {
            Log.e(TAG, "Cropping current bitmap. Can't perform this action right now.");
            return true;
        }

        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            if (mGestureCallback != null) {
                mGestureCallback.onGestureStarted();
            }
        }

        mScaleDetector.onTouchEvent(event);

        if (!mScaleDetector.isInProgress()) {
            mGestureDetector.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:

                if(mGestureCallback != null) {
                    mGestureCallback.onGestureCompleted();
                }

                return onUp();

        }

        return true;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {

        if (isCropping) {
            Log.e(TAG, "Cropping current bitmap. Can't set bitmap now");
            return;
        }

        mFirstRender = true;
        if (bm == null) {
            mBitmap = null;
            super.setImageBitmap(null);
            return;
        }

        if (bm.getHeight() > 1280 || bm.getWidth() > 1280) {
            Log.w(TAG, "Bitmap size greater than 1280. This might cause memory issues");
        }

        mBitmap = bm;

        if (doPreScaling) {
            int max_param = Math.max(bm.getWidth(), bm.getHeight());
            mPreScale = (float) max_param / (float) getWidth();

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() / mPreScale),
                    (int) (bm.getHeight() / mPreScale), false);
            super.setImageBitmap(scaledBitmap);
        } else {
            mPreScale = 1f;
            super.setImageBitmap(bm);
        }
        requestLayout();
    }

    public void replaceBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("Can not replace with null bitmap");
        }

        if (mBitmap == null) {
            setImageBitmap(bitmap);
            return;
        }

        if (mBitmap.getWidth() != bitmap.getWidth() || mBitmap.getHeight() != bitmap.getHeight()) {
            Log.e(TAG, "Bitmap is of different size. Not replacing");
            return;
        }

        super.setImageBitmap(bitmap);
        mBitmap = bitmap;
    }

    private void cropToCenter(Drawable drawable, int frameDimen) {

        if (drawable == null) {
            if (DEBUG) {
                Log.e(TAG, "Drawable is null. I can't fit anything");
            }
            return;
        }

        if (frameDimen == 0) {
            if (DEBUG) {
                Log.e(TAG, "Frame Dimension is 0. I'm quite boggled by it.");
            }
            return;
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (DEBUG) {
            Log.i(TAG, "drawable size: (" + width + " ," + height + ")");
        }

        int min_dimen = Math.min(width, height);
        float scaleFactor = (float)min_dimen/(float)frameDimen;

        Matrix matrix = new Matrix();
        matrix.setScale(1f / scaleFactor, 1f / scaleFactor);
        matrix.postTranslate((frameDimen - width / scaleFactor) / 2,
                (frameDimen - height / scaleFactor) / 2);
        setImageMatrix(matrix);
    }

    private void fitToCenter(Drawable drawable, int frameDimen) {

        if (drawable == null) {
            if (DEBUG) {
                Log.e(TAG, "Drawable is null. I can't fit anything");
            }
            return;
        }

        if (frameDimen == 0) {
            if (DEBUG) {
                Log.e(TAG, "Frame Dimension is 0. I'm quite boggled by it.");
            }
            return;
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (DEBUG) {
            Log.i(TAG, "drawable size: (" + width + " ," + height + ")");
        }

        int min_dimen = Math.max(width, height);
        float scaleFactor = (float)min_dimen/(float)frameDimen;

        Matrix matrix = new Matrix();
        matrix.setScale(1f / scaleFactor, 1f / scaleFactor);
        matrix.postTranslate((frameDimen - width / scaleFactor) / 2,
                (frameDimen - height / scaleFactor) / 2);
        setImageMatrix(matrix);

        // If over scrolled, return back to the place.
        float tx = getMatrixValue(matrix, Matrix.MTRANS_X);
        float ty = getMatrixValue(matrix, Matrix.MTRANS_Y);
        float scaleX = getMatrixValue(matrix, Matrix.MSCALE_X);
        if (scaleX < mMinZoom) {
            float xx = getWidth() / 2 - mMinZoom * drawable.getIntrinsicWidth() / 2;
            float yy = getHeight() / 2 - mMinZoom * drawable.getIntrinsicHeight() / 2;
            animateAdjustmentWithScale(tx, xx, ty, yy, scaleX, mMinZoom);
        }
    }

    public boolean onScroll(float distanceX, float distanceY) {

        Matrix matrix = getImageMatrix();
        matrix.postTranslate(-distanceX, -distanceY);
        setImageMatrix(matrix);

        invalidate();
        return true;
    }

    private boolean onUp() {

        Drawable drawable = getDrawable();
        if (drawable == null) {
            return false;
        }

        // If over scrolled, return back to the place.
        Matrix matrix = getImageMatrix();
        float tx = getMatrixValue(matrix, Matrix.MTRANS_X);
        float ty = getMatrixValue(matrix, Matrix.MTRANS_Y);
        float scaleX = getMatrixValue(matrix, Matrix.MSCALE_X);
        float scaleY = getMatrixValue(matrix, Matrix.MSCALE_Y);

        if (DEBUG) {
            Log.i(TAG, "onUp: " + tx + " " + ty);
            Log.i(TAG, "scale: " + scaleX);
            Log.i(TAG, "min, max, base zoom: " + mMinZoom + ", " + mMaxZoom + ", " + mBaseZoom);
            Log.i(TAG, "imageview size: " + getWidth() + " " + getHeight());
            Log.i(TAG, "drawable size: " + drawable.getIntrinsicWidth() + " " + drawable.getIntrinsicHeight());
            Log.i(TAG, "scaled drawable size: " + scaleX * drawable.getIntrinsicWidth() + " " + scaleY * drawable.getIntrinsicHeight());
            Log.i(TAG, "h diff: " + (scaleY * drawable.getIntrinsicHeight() + ty - getHeight()));
        }

        if (scaleX < mMinZoom && mMinZoom >= mBaseZoom) {
            if (DEBUG) {
                Log.i(TAG, "set scale to min zoom: " + mMinZoom);
            }

            float xx = getWidth()/2 - mMinZoom * drawable.getIntrinsicWidth()/2;
            float yy = getHeight()/2 - mMinZoom * drawable.getIntrinsicHeight()/2;

            if (drawable.getIntrinsicHeight() > drawable.getIntrinsicWidth()) {
                if (ty >= 0) {
                    yy = 0;
                } else if (ty + scaleY * drawable.getIntrinsicHeight() <= getHeight()) {
                    yy = getHeight() - mMinZoom * drawable.getIntrinsicHeight();
                } else {
                    // We want to change scale based on P1. P1 is (x, mFocusY). x does not matter here. Consider 1D.
                    // We translate P1 to origin (x, mFocusY) -> (x, 0)
                    // Apply zoom on origin -> (minzoom/scale)
                    // Translate origin to P1
                    yy = (ty - mFocusY) * (mMinZoom/scaleY) + mFocusY;
                }
            } else {
                if (tx >= 0) {
                    xx = 0;
                } else if (tx + scaleX * drawable.getIntrinsicWidth() <= getWidth()) {
                    xx = getWidth() - mMinZoom * drawable.getIntrinsicWidth();
                } else {
                    // We want to change scale based on P1. P1 is (mFocusX, y). y does not matter here. Consider 1D.
                    // We translate P1 to origin (mFocusX, y) -> (0, y)
                    // Apply zoom on origin -> (minzoom/scale)
                    // Translate origin to P1
                    xx = (tx - mFocusX) * (mMinZoom/scaleX) + mFocusX;
                }
            }

            if (showAnimation()) {

                animateAdjustmentWithScale(tx, xx, ty, yy, scaleX, mMinZoom);

            } else {
                matrix.reset();
                matrix.setScale(mMinZoom, mMinZoom);
                matrix.postTranslate(xx, yy);
                setImageMatrix(matrix);
                invalidate();
                if (DEBUG) {
                    Log.i(TAG, "scale after invalidate: " + getScale(matrix));
                }
            }
            return true;
        } else if (scaleX <= mBaseZoom || scaleX <= mBaseZoomBigger) {

            // align to center for the smaller dimension
            int h = drawable.getIntrinsicHeight();
            int w = drawable.getIntrinsicWidth();

            float xTranslate;
            float yTranslate;

            if(h <= w) {
                yTranslate = getHeight()/2 - scaleX * h/2;

                if (tx >= 0) {
                    xTranslate = 0;
                } else {
                    float xDiff = getWidth() - (scaleX) * drawable.getIntrinsicWidth();
                    if (tx < xDiff) {
                        xTranslate = xDiff;
                    } else {
                        xTranslate = tx;
                    }
                }

            } else {
                xTranslate = getWidth()/2 - scaleX * w/2;

                if(ty >= 0) {
                    yTranslate = 0;
                } else {
                    float yDiff = getHeight() - (scaleY) * drawable.getIntrinsicHeight();
                    if (ty < yDiff) {
                        yTranslate = yDiff;
                    } else {
                        yTranslate = ty;
                    }
                }
            }

            if (showAnimation()) {
                matrix.reset();
                matrix.postScale(scaleX, scaleX);
                matrix.postTranslate(tx, ty);
                setImageMatrix(matrix);

                animateAdjustment(xTranslate - tx, yTranslate - ty);

            } else {
                matrix.reset();
                matrix.postScale(scaleX, scaleX);
                matrix.postTranslate(xTranslate, yTranslate);
                setImageMatrix(matrix);
                invalidate();
            }

            return true;

        } else if (isMaxZoomSet && scaleX > mMaxZoom) {

            if(DEBUG) {
                Log.i(TAG, "set to max zoom");
                Log.i(TAG, "isMaxZoomSet: " + isMaxZoomSet);
            }

            if (showAnimation()) {
                animateOverMaxZoomAdjustment();
//                adjustToSides();
            } else {
                matrix.postScale(mMaxZoom / scaleX, mMaxZoom / scaleX, mFocusX, mFocusY);
                setImageMatrix(matrix);
                invalidate();
                adjustToSides();
            }
            return true;
        }

        if (DEBUG) {
            Log.i(TAG, "adjust to sides");
        }
        adjustToSides();
        return true;
    }

    private boolean adjustToSides() {
        boolean changeRequired = false;

        Drawable drawable = getDrawable();
        if (drawable == null) {
            return false;
        }

        Matrix matrix = getImageMatrix();

        float tx = getMatrixValue(matrix, Matrix.MTRANS_X);
        float ty = getMatrixValue(matrix, Matrix.MTRANS_Y);
        float scaleX = getMatrixValue(matrix, Matrix.MSCALE_X);
        float scaleY = getMatrixValue(matrix, Matrix.MSCALE_Y);

        if (tx > 0) {
            tx = -tx;
            changeRequired = true;
        } else {

            // check if scrolled to left
            float xDiff = getWidth() - (scaleX) * drawable.getIntrinsicWidth();
            if (tx < xDiff) {
                tx = xDiff - tx;
                changeRequired = true;
            } else {
                tx = 0;
            }
        }

        if (ty > 0) {
            ty = -ty;
            changeRequired = true;
        } else {

            // check if scrolled to top
            float yDiff = getHeight() - (scaleY) * drawable.getIntrinsicHeight();
            if (ty < yDiff) {
                ty = yDiff - ty;
                changeRequired = true;
            } else {
                ty = 0;
            }
        }

        if (changeRequired) {

            if (showAnimation()) {
                animateAdjustment(tx, ty);
            } else {
                matrix.postTranslate(tx, ty);
                setImageMatrix(matrix);
                invalidate();
            }
        }

        return changeRequired;
    }

    private float getMatrixValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    private float getScale(Matrix matrix) {
        return getMatrixValue(matrix, Matrix.MSCALE_X);
    }

    public boolean isDoPreScaling() {
        return doPreScaling;
    }

    public void setDoPreScaling(boolean doPreScaling) {
        this.doPreScaling = doPreScaling;
    }

    public float getMaxZoom() {
        return mMaxZoom;
    }

    public void setMaxZoom(float zoom) {

        if (zoom <= 0) {
            Log.e(TAG, "Max zoom must be greater than 0");
            return;
        }

        if (mMinZoom > 0 && zoom < mMinZoom) {
            Log.e(TAG, "Max zoom must be greater than min zoom");
            return;
        }

        this.mMaxZoom = zoom;
        isMaxZoomSet = true;
    }

    public float getMinZoom() {
        return mMinZoom;
    }

    public void setMinZoom(float zoom) {
        if (_setMinZoom(zoom)) {
            isMinZoomSetByUser = true;
        }
    }

    private boolean _setMinZoom(float zoom) {
        if (zoom <= 0) {
            Log.e(TAG, "Min zoom must be greater than 0");
            return false;
        }

        if (isMaxZoomSet && zoom > mMaxZoom) {
            Log.e(TAG, "Min zoom must not be greater than max zoom");
            return false;
        }

        isMinZoomSetByUser = false;
        this.mMinZoom = zoom;
        return true;
    }

    public void cropToCenter() {

        if (isCropping) {
            Log.e(TAG, "Cropping current bitmap. Can't perform this action right now.");
            return;
        }

        Drawable drawable = getDrawable();
        if (drawable != null) {
            cropToCenter(drawable, getWidth());
        }
    }

    public void fitToCenter() {

        if (isCropping) {
            Log.e(TAG, "Cropping current bitmap. Can't perform this action right now.");
            return;
        }

        Drawable drawable = getDrawable();
        if (drawable != null) {
            fitToCenter(drawable, getWidth());
        }
    }

    public Bitmap cropBitmap() throws OutOfMemoryError {

        if (isCropping) {
            Log.e(TAG, "Cropping current bitmap. Can't perform this action right now.");
            return null;
        }

        isCropping = true;
        try {
            Bitmap bitmap = getCroppedBitmap();
            isCropping = false;
            return bitmap;
        } catch (OutOfMemoryError e) {
            isCropping = false;
            throw e;
        }

    }

    public CropInfo getCropInfo() {
        if (mBitmap == null) {
            Log.e(TAG, "original image is not available");
            return null;
        }

        Matrix matrix = getImageMatrix();

        float xTrans = getMatrixValue(matrix, Matrix.MTRANS_X);
        float yTrans = getMatrixValue(matrix, Matrix.MTRANS_Y);
        float scale = getMatrixValue(matrix, Matrix.MSCALE_X);

        if (DEBUG) {
            Log.i(TAG, "xTrans: " + xTrans + ", yTrans: " + yTrans + " , scale: " + scale);
            Log.i(TAG, "old bitmap: " + mBitmap.getWidth() + " " + mBitmap.getHeight());
        }

        // No scale/crop required.
        if (xTrans > 0 && yTrans > 0 && scale <= mMinZoom) {
            // Add padding if not square
            int verticalPadding = mBitmap.getHeight() > mBitmap.getWidth() ? 0 : (mBitmap.getWidth() - mBitmap.getHeight()) / 2;
            int horizontalPadding = mBitmap.getWidth() > mBitmap.getHeight() ? 0 : (mBitmap.getHeight() - mBitmap.getWidth()) / 2;
            return CropInfo.cropCompleteBitmap(mBitmap, scale, mAddPaddingToMakeSquare, horizontalPadding, verticalPadding);
        }

        float cropY = - yTrans / scale;
        float Y = getHeight() / scale;
        float cropX = -xTrans / scale;
        float X = getWidth() / scale;

        if (DEBUG) {
            Log.i(TAG, "cropY: " + cropY);
            Log.i(TAG, "Y: " + Y);
            Log.i(TAG, "cropX: " + cropX);
            Log.i(TAG, "X: " + X);
        }

        if (cropY + Y > mBitmap.getHeight()) {
            cropY = mBitmap.getHeight() - Y;
            if (DEBUG) {
                Log.i(TAG, "readjust cropY to: " + cropY);
            }
        }  else if (cropY < 0) {
            cropY = 0;
            if (DEBUG) {
                Log.i(TAG, "readjust cropY to: " + cropY);
            }
        }

        if (cropX + X > mBitmap.getWidth()) {
            cropX = mBitmap.getWidth() - X;
            if (DEBUG) {
                Log.i(TAG, "readjust cropX to: " + cropX);
            }
        } else if (cropX < 0) {
            cropX = 0;
            if (DEBUG) {
                Log.i(TAG, "readjust cropX to: " + cropX);
            }
        }

        Rect rect;
        int horizontalPadding = 0;
        int verticalPadding = 0;

        boolean isPaddingRequired = true;

        if (mBitmap.getHeight() > mBitmap.getWidth()) {
            // Height is greater than width.
            if (xTrans >= 0) {
                // Image is zoomed. Crop from height
                rect = new Rect(0, (int)cropY, mBitmap.getWidth(), (int)(Y + cropY));
                horizontalPadding = (int) ((Y - mBitmap.getWidth()) / 2);
            } else {
                // Crop from width and height both
                rect = new Rect((int)cropX, (int)cropY, (int)(cropX + X), (int)(cropY + Y));
                isPaddingRequired = false;
            }
        } else {
            if (DEBUG) {
                Log.i(TAG, "width > height");
            }

            if (yTrans >= 0) {
                // Image is zoomed. Crop from width and add padding to make square
                rect = new Rect((int)cropX, 0, (int)(cropX + X), mBitmap.getHeight());
                verticalPadding = (int) ((X - mBitmap.getHeight()) / 2);

                if (DEBUG) {
                    Log.i(TAG, "yTrans >= 0 " + yTrans);
                    Log.i(TAG, "rect: " + rect);
                    Log.i(TAG, "vertical padding: " + verticalPadding);
                }

            } else {
                // Crop from width and height both.
                rect = new Rect((int)cropX, (int)cropY, (int)(cropX + X), (int)(cropY + Y));
                isPaddingRequired = false;

                if (DEBUG) {
                    Log.i(TAG, "yTrans < 0 " + yTrans);
                    Log.i(TAG, "rect: " + rect);
                }

            }
        }

        return CropInfo.cropFromRect(rect, scale, mAddPaddingToMakeSquare && isPaddingRequired, horizontalPadding, verticalPadding);
    }

    public Bitmap getCroppedBitmap(CropInfo cropInfo) throws OutOfMemoryError {
        if (mBitmap == null) {
            Log.e(TAG, "original image is not available");
            return null;
        }

        if (!cropInfo.addPadding) {
            return Bitmap.createBitmap(mBitmap, cropInfo.x, cropInfo.y, cropInfo.width, cropInfo.height);
        }

        return BitmapUtils.addPadding(mBitmap, cropInfo, mPaintColor);
    }

    private Bitmap getCroppedBitmap() throws OutOfMemoryError {
        if (mBitmap == null) {
            Log.e(TAG, "original image is not available");
            return null;
        }

        Matrix matrix = getImageMatrix();

        if (doPreScaling) {
            matrix.postScale(1/mPreScale, 1/mPreScale);
        }

        float xTrans = getMatrixValue(matrix, Matrix.MTRANS_X);
        float yTrans = getMatrixValue(matrix, Matrix.MTRANS_Y);
        float scale = getMatrixValue(matrix, Matrix.MSCALE_X);

        if (DEBUG) {
            Log.i(TAG, "xTrans: " + xTrans + ", yTrans: " + yTrans + " , scale: " + scale);
        }

        // Load bitmap which is mutable
        Bitmap bitmap = null;

        if (DEBUG) {
            Log.i(TAG, "old bitmap: " + mBitmap.getWidth() + " " + mBitmap.getHeight());
        }

        if (xTrans > 0 && yTrans > 0 && scale <= mMinZoom) {
            // No scale/crop required.
            // Add padding if not square
            if (mAddPaddingToMakeSquare) {

                try {
                    return BitmapUtils.addPadding(mBitmap, mPaintColor);
                } catch (OutOfMemoryError e) {
                    Log.d(TAG, "OOM while adding padding");
                    throw e;
                }

            } else {
                return mBitmap;
            }

        } else {

            float cropY = - yTrans / scale;
            float Y = getHeight() / scale;
            float cropX = -xTrans / scale;
            float X = getWidth() / scale;

            if (DEBUG) {
                Log.i(TAG, "cropY: " + cropY);
                Log.i(TAG, "Y: " + Y);
                Log.i(TAG, "cropX: " + cropX);
                Log.i(TAG, "X: " + X);
            }

            if (cropY + Y > mBitmap.getHeight()) {
                cropY = mBitmap.getHeight() - Y;
                if (DEBUG) {
                    Log.i(TAG, "readjust cropY to: " + cropY);
                }
            }  else if (cropY < 0) {
                cropY = 0;
                if (DEBUG) {
                    Log.i(TAG, "readjust cropY to: " + cropY);
                }
            }

            if (cropX + X > mBitmap.getWidth()) {
                cropX = mBitmap.getWidth() - X;
                if (DEBUG) {
                    Log.i(TAG, "readjust cropX to: " + cropX);
                }
            } else if (cropX < 0) {
                cropX = 0;
                if (DEBUG) {
                    Log.i(TAG, "readjust cropX to: " + cropX);
                }
            }

            if (mBitmap.getHeight() > mBitmap.getWidth()) {
                // Height is greater than width.
                if (xTrans >= 0) {
                    // Image is zoomed. Crop from height

                    Rect src = new Rect(0, (int)cropY, mBitmap.getWidth(), (int)(Y + cropY));

                    if (mAddPaddingToMakeSquare) {
                        // Crop and add padding to the same bitmap
                        try {
                            int size = (int) Y;
                            bitmap = Bitmap.createBitmap(size, size, mBitmap.getConfig());
                            Canvas canvas = new Canvas(bitmap);
                            canvas.drawColor(mPaintColor);

                            // Put cropped bitmap into the canvas
                            int left = (size - mBitmap.getWidth()) / 2;
                            Rect dest = new Rect(left, 0, left + mBitmap.getWidth(), size);

                            canvas.drawBitmap(mBitmap, src, dest, null);
                        } catch (OutOfMemoryError e) {
                            if (bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                                bitmap = null;
                                throw e;
                            }
                        }

                    } else {

                        try {
                            bitmap = Bitmap.createBitmap(mBitmap, 0, (int) cropY, mBitmap.getWidth(), (int) Y,
                                    null, true);
                        } catch (OutOfMemoryError e) {
                            bitmap = null;
                            throw e;
                        }
                    }

                } else {
                    // Crop from width and height both
                    try {
                        bitmap = Bitmap.createBitmap(mBitmap, (int) cropX, (int) cropY, (int) X, (int) Y,
                                null, true);

                    } catch (OutOfMemoryError e) {
                        bitmap = null;
                        throw e;
                    }
                }
            } else {
                if (yTrans >= 0) {
                    // Image is zoomed. Crop from width and add padding to make square

                    Rect src = new Rect((int)cropX, 0, (int)(cropX + X), mBitmap.getHeight());

                    if (mAddPaddingToMakeSquare) {
                        try {
                            // Crop and add padding to the same bitmap
                            int size = (int) X;
                            bitmap = Bitmap.createBitmap(size, size, mBitmap.getConfig());
                            Canvas canvas = new Canvas(bitmap);
                            canvas.drawColor(mPaintColor);

                            // Put cropped bitmap into the canvas
                            int top = (size - mBitmap.getHeight()) / 2;
                            Rect dest = new Rect(0, top, size, top + mBitmap.getHeight());

                            canvas.drawBitmap(mBitmap, src, dest, null);
                        } catch (OutOfMemoryError e) {
                            if (bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                                bitmap = null;
                                throw e;
                            }
                        }
                    } else {

                        try {
                            bitmap = Bitmap.createBitmap(mBitmap, (int) cropX, 0, (int) X, mBitmap.getHeight(),
                                    null, true);
                        } catch (OutOfMemoryError e) {
                            bitmap = null;
                            throw e;
                        }
                    }

                } else {
                    // Crop from width and height both.

                    try {
                        bitmap = Bitmap.createBitmap(mBitmap, (int) cropX, (int) cropY, (int) X, (int) Y,
                                null, true);
                    } catch (OutOfMemoryError e) {
                        bitmap = null;
                        throw e;
                    }

                }

                if (DEBUG) {
                    Log.i(TAG, "width should be: " + mBitmap.getWidth());
                    if (bitmap != null) {
                        Log.i(TAG, "crop bitmap: " + bitmap.getWidth() + " " + bitmap.getHeight());
                    }
                }
            }
        }

        return bitmap;
    }

    public boolean showAnimation() {
        return showAnimation;
    }

    public void setShowAnimation(boolean showAnimation) {
        this.showAnimation = showAnimation;
    }

    public int getPaddingColor() {
        return mPaintColor;
    }

    public void setPaddingColor(int mPaintColor) {
        this.mPaintColor = mPaintColor;
    }

    public boolean isMakeSquare() {
        return mAddPaddingToMakeSquare;
    }

    public void setMakeSquare(boolean mAddPaddingToMakeSquare) {
        this.mAddPaddingToMakeSquare = mAddPaddingToMakeSquare;
    }

    public boolean isGestureEnabled() {
        return gestureEnabled;
    }

    public void setGestureEnabled(boolean gestureEnabled) {
        this.gestureEnabled = gestureEnabled;
    }

    public boolean isInitWithFitToCenter() {
        return initWithFitToCenter;
    }

    public void setInitWithFitToCenter(boolean initWithFitToCenter) {
        this.initWithFitToCenter = initWithFitToCenter;
    }

    public void release() {

        if (isCropping) {
            Log.e(TAG, "Cropping current bitmap. Can't perform this action right now.");
            return;
        }

        setImageBitmap(null);
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }

    // Scroll and Gesture Listeners
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!gestureEnabled) {
                return false;
            }

            if (isCropping) {
                Log.e(TAG, "Cropping current bitmap. Can't perform this action right now.");
                return false;
            }

            if (e1 == null || e2 == null) {
                return false;
            }
            if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
                return false;
            }

            CropperImageView.this.onScroll(distanceX, distanceY);
            return false;
        }

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        protected boolean mScaled = false;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!gestureEnabled) {
                return false;
            }

            if (isCropping) {
                Log.e(TAG, "Cropping current bitmap. Can't perform this action right now.");
                return false;
            }

            Matrix matrix = getImageMatrix();
            // This does the trick!
            mFocusX = detector.getFocusX();
            mFocusY = detector.getFocusY();

            matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(),
                    detector.getFocusX(), detector.getFocusY());

            setImageMatrix(matrix);
            invalidate();
            return true;
        }
    }

    public void setGestureCallback(GestureCallback mGestureCallback) {
        this.mGestureCallback = mGestureCallback;
    }

    public interface GestureCallback {
        void onGestureStarted();
        void onGestureCompleted();
    }

    private void animateAdjustmentWithScale(final float xStart, final float xEnd,
                                            final float yStart, final float yEnd,
                                            final float scaleStart, final float scaleEnd) {
        ValueAnimator animator = ValueAnimator.ofInt(0, 20);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Matrix matrix = getImageMatrix();
                matrix.reset();

                Integer value = (Integer)animation.getAnimatedValue();

                matrix.postScale((scaleEnd - scaleStart) * value / 20f + scaleStart,
                        (scaleEnd - scaleStart) * value / 20f + scaleStart);
                matrix.postTranslate((xEnd - xStart) * value / 20f + xStart,
                        (yEnd - yStart) * value / 20f + yStart);

                setImageMatrix(matrix);
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAdjusting = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAdjusting = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAdjusting = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isAdjusting = true;
            }
        });

        animator.start();
    }

    private void animateAdjustment(final float xDiff, final float yDiff) {
        ValueAnimator animator = ValueAnimator.ofInt(0, 20);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Matrix matrix = getImageMatrix();
                matrix.postTranslate(xDiff / 20, yDiff / 20);
                setImageMatrix(matrix);
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAdjusting = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAdjusting = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAdjusting = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isAdjusting = true;
            }
        });

        animator.start();
    }

    private void animateOverMaxZoomAdjustment() {

        Matrix matrix = getImageMatrix();
        final float scale = getScale(matrix);

        final ValueAnimator animator = ValueAnimator.ofInt(0, 20);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Matrix matrix = getImageMatrix();

                float currentScale = getScale(matrix);
                if (currentScale <= mMaxZoom) {
//                    animator.cancel();
                    return;
                }

                double expScale = Math.pow(mMaxZoom / scale, 1 / 20f);
                matrix.postScale((float)expScale, (float)expScale, mFocusX, mFocusY);
                setImageMatrix(matrix);
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAdjusting = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAdjusting = false;
                adjustToSides();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAdjusting = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isAdjusting = true;
            }
        });

        animator.start();
    }
}
