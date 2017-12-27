## Cropper - NoCropper

This is a lightweight Image Cropper for Android which also supports no-crop feature.

**[Project Page](http://www.jayrambhia.com/project/nocropper-library)**
**[Blogpost](http://www.jayrambhia.com/blog/instagram-cropper)**

Here's a short gif showing how it works.

![Demo](https://raw.githubusercontent.com/jayrambhia/CropperNoCropper/master/art/demo1.gif)

And, here's a bit longer [YouTube Video](https://youtu.be/OoYSt2vtdNs)

## CropperView

It's a FrameLayout which contains a view for Grid and an imageview. This project supports only square cropping.
CropperView contains some basic methods like `setImageBitmap()`, `setMaxZoom()`, `setMinZoom()`, etc which are
forwarded to `CropperImageView`.

It's not an Activity or Fragment. It's just a FrameLayout which you can use anywhere and however you want in your app.
There are some styling and customizations also available.

## How To Install

### Maven

    repositories {
        maven {
            url  "http://dl.bintray.com/jayrambhia/maven"
        }
    }

### JCenter

    repositories {
        jcenter()
    }

### Dependency

    dependencies {
        compile 'com.fenchtose.nocropper:nocropper:0.2.1'
    }

## CropperImageView

It's a square ImageView which acts as the cropper. It tries to keep the image in the range of max and min zoom.
It automatically adjusts the position of the image, if it's zoomed out.

### How To Use:

    <com.fenchtose.nocropper.CropperView
        android:background="#ff282828"
        android:id="@+id/cropper_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:nocropper__grid_opacity="0.8"
        app:nocropper__grid_thickness="0.8dp"
        app:nocropper__grid_color="@color/colorAccent"
        app:nocropper__padding_color="#ff282828"/>
 
And that's it. `CropperView` is ready to be used anywhere in the app. No dependencies.        


### Useful Methods:

 - `setMaxZoom(float zoom)` set Maximum zoom
 - `setMinZoom(float zoom)` set Minimum zoom
 - `setImageBitmap(Bitmap bm)` set Bitmap
 - `replaceBitmap(Bitmap bm)` Replace Bitmap without changing the image matrix
 - `setGestureEnabled(boolean enabled)` Enable/Disable Cropper gestures
 - `setDebug(boolean debug)` - Debugging mode
 - `cropToCenter()` - Set Image in the center with square crop view
 - `fitToCenter()` - Fit Image in the center (no cropping view)
 - `setPaddingColor(int color)` - Set Color of square image padding
 - `setMakeSquare(boolean status)` - If you want to add padding in the cropped image (if cropped image is not square)
 - `isMakeSquare()` - Check if cropper will give a square image or not
 - `initWithFitToCenter(boolean fitToCenter)` - Cropper will fit image to center instead of cropping to center when bitmap is set.
 - `getCroppedBitmap()` - Get Cropped Bitmap - returns BitmapResult. Bitmap may be null if the cropper is unable to crop. If the user is in mid-gesture, it will return BitmapResult with null bitmap and State as FAILURE_GESTURE_IN_PROCESS
 - `getCroppedBitmapAsync(CropperCallback callback)` - Crop bitmap in background thread and get result via `CropperCallback`. - returns BitmapResult.State. If the user is in mid-gesture, it will return State as FAILURE_GESTURE_IN_PROCESS else it will return STARTED to indicate that the process has been started.
 - `release()` - Remove and Recycle Bitmap
 - `setGridCallback(GridCallback callback)` - More control to you about when you want to show the grid.


### Styleables

 - `nocropper__grid_color` - Color of the grid
 - `nocropper__grid_thickness` - Thickness of grid lines
 - `nocropper__grid_opacity` - Opacity of grid lines
 - `nocropper__padding_color` - Color of the image padding
 - `nocropper__add_padding_to_make_square` - boolean
 - `nocropper__fit_to_center` - boolean - Fit image to center instead of crop when you set a bitmap

### CropperCallback

It's an abstract class for callback. The callback methods will be invoked on main ui thread. It has following methods.

 - onStarted() - invoked when cropping is started.
 - onCropped(Bitmap bitmap) - invoked when cropped result is available.
 - onOutOfMemoryError() - invoked when the cropper encounters OOM error while cropping.

# GridCallback

It's an interface class for callback. You can control when you want to show this based on this class. It has following methods.

 - onGestureStarted() - invoked when user starts a gesture. Return true if you want to show the grid. Return false if you want to hide the grid.
 - onGestureCompleted() - invoked when completes the gesture. Return true if you want to show the grid. Return false if you want to hide the grid.

### 0.1 to 0.2 update note:

All the styleables are renamed to have prefix `nocropper__` so as not to have collision issues with other resource attributes. By collision I mean, your app will not
build if it has to resources attributes with same name.

## Licenses and Release History

**[CHANGELOG](https://github.com/jayrambhia/CropperNoCropper/blob/master/Changelog.md)**

NoCropper binaries and source code can be used according to the [Apache License, Version 2.0](https://github.com/jayrambhia/CropperNoCropper/blob/master/License).

