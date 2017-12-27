package com.fenchtose.nocropper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jay Rambhia on 11/4/2015.
 */
class CropperGridView extends View {

    private static final String TAG = "CropperGridView";

    private long HIDE_INTERVAL = 1500;
    private Paint mPaint;
    private int mColor = 0xfffffff;
    private int mAlpha = 200;
    private int mStrokeWidth = 3;

    private boolean showGrid = false;

    private Handler mHandler;

    private Path mPath;

    public CropperGridView(Context context) {
        super(context);
        init(context, null);
    }

    public CropperGridView(Context context, AttributeSet attrs) {
        super(context);
        init(context, attrs);
    }

    public CropperGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CropperGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context);
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

        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.nocropper__CropperView);
            mColor = mTypedArray.getColor(R.styleable.nocropper__CropperView_nocropper__grid_color, mColor);

            float alpha = 255 * mTypedArray.getFloat(R.styleable.nocropper__CropperView_nocropper__grid_opacity, 1f);
            if (alpha < 0) {
                alpha = 0;
            } else if (alpha > 255) {
                alpha = 255;
            }
            mAlpha = (int)alpha;

            mStrokeWidth = mTypedArray.getDimensionPixelOffset(R.styleable.nocropper__CropperView_nocropper__grid_thickness,
                    mStrokeWidth);

            mTypedArray.recycle();
        }

        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAlpha(mAlpha);

        mPath = new Path();

        mHandler = new Handler();

        if (isInEditMode()) {
            showGrid = true;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!showGrid) {
            return;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        mPath.reset();
        mPath.moveTo(width / 3, 0);
        mPath.lineTo(width / 3, height);
        mPath.moveTo(2 * width / 3, 0);
        mPath.lineTo(2*width/3, height);
        mPath.moveTo(0, height/3);
        mPath.lineTo(width, height/3);
        mPath.moveTo(0, 2*height/3);
        mPath.lineTo(width, 2*height/3);
        canvas.drawPath(mPath, mPaint);
    }

    public boolean showGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        if (this.showGrid != showGrid) {
            this.showGrid = showGrid;
            if (this.showGrid) {
                mHandler.removeCallbacks(mHideRunnable);
                invalidate();
                return;
            }

            mHandler.postDelayed(mHideRunnable, HIDE_INTERVAL);
        }
    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            showGrid = false;
            invalidate();
        }
    };
}
