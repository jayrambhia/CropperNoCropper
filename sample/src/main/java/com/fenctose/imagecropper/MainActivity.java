package com.fenctose.imagecropper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.Toast;

import com.fenchtose.nocropper.BitmapResult;
import com.fenchtose.nocropper.CropInfo;
import com.fenchtose.nocropper.CropMatrix;
import com.fenchtose.nocropper.CropResult;
import com.fenchtose.nocropper.CropState;
import com.fenchtose.nocropper.CropperCallback;
import com.fenchtose.nocropper.CropperView;
import com.fenchtose.nocropper.ScaledCropper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_PERMISSION = 22;
    private static final int REQUEST_GALLERY = 21;
    private static final String TAG = "MainActivity";
    private static final String CroppedImagesPath = Environment.getExternalStorageDirectory().toString();

    CropperView mImageView;
    CheckBox originalImageCheckbox;
    CheckBox cropAsyncCheckbox;
    CheckBox keepCropState;

    private Bitmap originalBitmap;
    private Bitmap mBitmap;
    private boolean isSnappedToCenter = false;

    private int rotationCount = 0;

    private HashMap<String, CropMatrix> matrixMap = new HashMap<>();
    private CropMatrix lastCropMatrix = null;
    private String currentFilePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main_portrait);
        } else {
            Log.i(TAG, "Set landscape mode");
            setContentView(R.layout.activity_main_landscape);
        }

        mImageView = findViewById(R.id.imageview);
        originalImageCheckbox = findViewById(R.id.original_checkbox);
        cropAsyncCheckbox = findViewById(R.id.crop_checkbox);
        keepCropState = findViewById(R.id.keep_crop_state);

        findViewById(R.id.image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGalleryIntent();
            }
        });

        findViewById(R.id.crop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageCropClicked();
            }
        });

        findViewById(R.id.rotate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();
            }
        });

        findViewById(R.id.snap_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapImage();
            }
        });

        findViewById(R.id.gesture_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGestures();
            }
        });

        mImageView.setDebug(true);

        mImageView.setGridCallback(new CropperView.GridCallback() {
            @Override
            public boolean onGestureStarted() {
                return true;
            }

            @Override
            public boolean onGestureCompleted() {
                return false;
            }
        });


    }

    public void onImageButtonClicked() {
        startGalleryIntent();
    }

    public void onImageCropClicked() {
        if (cropAsyncCheckbox.isChecked()) {
            cropImageAsync();
        } else {
            cropImage();
        }
    }

    public void toggleGestures() {
        boolean enabled = mImageView.isGestureEnabled();
        enabled = !enabled;
        mImageView.setGestureEnabled(enabled);
        Toast.makeText(this, "Gesture " + (enabled ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
    }

    private void loadNewImage(String filePath) {
        this.currentFilePath = filePath;
        rotationCount = 0;
        Log.i(TAG, "load image: " + filePath);
        mBitmap = BitmapFactory.decodeFile(filePath);
        originalBitmap = mBitmap;
        Log.i(TAG, "bitmap: " + mBitmap.getWidth() + " " + mBitmap.getHeight());

        int maxP = Math.max(mBitmap.getWidth(), mBitmap.getHeight());
        float scale1280 = (float) maxP / 1280;
        Log.i(TAG, "scaled: " + scale1280 + " - " + (1 / scale1280));

        calculateBitMapMaxZoom();

        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) (mBitmap.getWidth() / scale1280),
                (int) (mBitmap.getHeight() / scale1280), true);

        mImageView.setImageBitmap(mBitmap);

        loadCachedCropMatrix();
    }

    private void loadCachedCropMatrix() {
        CropMatrix cropMatrix = matrixMap.get(currentFilePath);
        if (cropMatrix == null && keepCropState.isChecked()){
            cropMatrix = lastCropMatrix;
        }
        final CropMatrix finalMatrix = cropMatrix;

        if (finalMatrix != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mImageView.setCropMatrix(finalMatrix, true);
                }
            }, 30);
        }
    }

    private void calculateBitMapMaxZoom() {
        CheckBox zoomLimit = findViewById(R.id.zoom_limit);

        if (zoomLimit.isChecked()) {

            float widthCoefficient = (float) mImageView.getWidth() / mBitmap.getWidth();
            float heightCoefficient = (float) mImageView.getHeight() / mBitmap.getHeight();

            float maxZoom = Math.max(heightCoefficient, widthCoefficient) / Math.min(heightCoefficient, widthCoefficient);

            mImageView.setMaxZoom(maxZoom - 0.15f);
        } else {
            if (mImageView.getWidth() != 0) {
                float maxZoom = mImageView.getWidth() * 2 / 1280f;
                mImageView.setMaxZoom(maxZoom);
            } else {

                ViewTreeObserver vto = mImageView.getViewTreeObserver();
                vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                        float maxZoom = mImageView.getWidth() * 2 / 1280f;
                        mImageView.setMaxZoom(maxZoom);
                        return true;
                    }
                });
            }
        }
    }

    private void startGalleryIntent() {

        if (currentFilePath != null) {
            matrixMap.put(currentFilePath, mImageView.getCropMatrix());
            lastCropMatrix = mImageView.getCropMatrix();
        }

        if (!hasGalleryPermission()) {
            askForGalleryPermission();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private boolean hasGalleryPermission() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void askForGalleryPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_READ_PERMISSION);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent resultIntent) {
        super.onActivityResult(requestCode, responseCode, resultIntent);

        if (responseCode == RESULT_OK) {
            String absPath = BitmapUtils.getFilePathFromUri(this, resultIntent.getData());
            loadNewImage(absPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryIntent();
                return;
            }
        }

        Toast.makeText(this, "Gallery permission not granted", Toast.LENGTH_SHORT).show();
    }

    private void cropImageAsync() {
        CropState state = mImageView.getCroppedBitmapAsync(new CropperCallback() {
            @Override
            public void onCropped(Bitmap bitmap) {
                if (bitmap != null) {

                    try {
                        String imagePath = CroppedImagesPath + "/crop_test.jpg";
                        BitmapUtils.writeBitmapToFile(bitmap, new File(imagePath), 90);
                        sendSuccessfullyMessage(imagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onOutOfMemoryError() {

            }
        });

        if (state == CropState.FAILURE_GESTURE_IN_PROCESS) {
            Toast.makeText(this, "unable to crop. Gesture in progress", Toast.LENGTH_SHORT).show();
        }

        if (originalImageCheckbox.isChecked()) {
            cropOriginalImageAsync();
        }
    }

    private void cropImage() {

        BitmapResult result = mImageView.getCroppedBitmap();

        if (result.getState() == CropState.FAILURE_GESTURE_IN_PROCESS) {
            Toast.makeText(this, "unable to crop. Gesture in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = result.getBitmap();

        if (bitmap != null) {
            Log.d("Cropper", "crop1 bitmap: " + bitmap.getWidth() + ", " + bitmap.getHeight());
            try {
                String imagePath = CroppedImagesPath + "/crop_test.jpg";
                BitmapUtils.writeBitmapToFile(bitmap, new File(imagePath), 90);
                sendSuccessfullyMessage(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (originalImageCheckbox.isChecked()) {
            cropOriginalImage();
        }

    }

    private void sendSuccessfullyMessage(String imagePath) {
        Toast.makeText(this, "Cropped image was stored in " + imagePath, Toast.LENGTH_SHORT).show();
    }

    private ScaledCropper prepareCropForOriginalImage() {
        CropResult result = mImageView.getCropInfo();
        if (result.getCropInfo() == null) {
            return null;
        }

        float scale;
        if (rotationCount % 2 == 0) {
            // same width and height
            scale = (float) originalBitmap.getWidth() / mBitmap.getWidth();
        } else {
            // width and height are interchanged
            scale = (float) originalBitmap.getWidth() / mBitmap.getHeight();
        }

        CropInfo cropInfo = result.getCropInfo().rotate90XTimes(mBitmap.getWidth(), mBitmap.getHeight(), rotationCount);
        return new ScaledCropper(cropInfo, originalBitmap, scale);
    }

    private void cropOriginalImage() {
        if (originalBitmap != null) {
            ScaledCropper cropper = prepareCropForOriginalImage();
            if (cropper == null) {
                return;
            }

            Bitmap bitmap = cropper.cropBitmap();
            if (bitmap != null) {
                try {
                    BitmapUtils.writeBitmapToFile(bitmap, new File(Environment.getExternalStorageDirectory() + "/crop_test_info_orig.jpg"), 90);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void cropOriginalImageAsync() {
        if (originalBitmap != null) {
            ScaledCropper cropper = prepareCropForOriginalImage();
            if (cropper == null) {
                return;
            }

            cropper.crop(new CropperCallback() {
                @Override
                public void onCropped(Bitmap bitmap) {
                    if (bitmap != null) {
                        try {
                            String imagePath = CroppedImagesPath + "/crop_test_info_orig.jpg";
                            BitmapUtils.writeBitmapToFile(bitmap, new File(imagePath), 90);
                            sendSuccessfullyMessage(imagePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void rotateImage() {
        if (mBitmap == null) {
            Log.e(TAG, "bitmap is not loaded yet");
            return;
        }

        mBitmap = BitmapUtils.rotateBitmap(mBitmap, 90);
        mImageView.setImageBitmap(mBitmap);
        rotationCount++;
    }

    private void snapImage() {
        if (isSnappedToCenter) {
            mImageView.cropToCenter();
        } else {
            mImageView.fitToCenter();
        }

        isSnappedToCenter = !isSnappedToCenter;
    }
}
