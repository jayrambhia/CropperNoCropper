## Cropper - NoCropper

[![](https://jitpack.io/v/jayrambhia/CropperNoCropper.svg)](https://jitpack.io/#jayrambhia/CropperNoCropper)

This is a lightweight Image Cropper for Android which also supports no-crop feature.

## Version 0.3.0 adds pre-scale support!

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

### JCenter / Bintray (Deprecated)

##### Maven

    repositories {
        maven {
            url  "http://dl.bintray.com/jayrambhia/maven"
        }
    }

or

##### JCenter

    repositories {
        jcenter()
    }

##### Dependency

    dependencies {
        compile 'com.fenchtose.nocropper:nocropper:0.3.1'
    }

### JitPack

    repositories {
        maven {
            url  "https://jitpack.io"
        }
    }
    
    dependencies {
        implementation 'com.github.jayrambhia:CropperNoCropper:0.3.2'
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
 - `getCroppedBitmapAsync(CropperCallback callback)` - Crop bitmap in background thread and get result via `CropperCallback`. - returns CropState. If the user is in mid-gesture, it will return State as FAILURE_GESTURE_IN_PROCESS else it will return STARTED to indicate that the process has been started.
 - `getCropInfo()` - Get `CropInfo` which you can use manually to crop the bitmap or use it to crop the original un-scaled bitmap.
 - `release()` - Remove and Recycle Bitmap
 - `setGridCallback(GridCallback callback)` - More control to you about when you want to show the grid.
 - `getCropMatrix()` - Get `CropMatrix` - Use this to restore the crop state of the image.
 - `setCropMatrix(CropMatrix matrix, boolean animate)` - Restore crop state of the image. If this is called right after `setImageBitmap`, it should be called with a delay.

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

### GridCallback

It's an interface class for callback. You can control when you want to show this based on this class. It has following methods.

 - onGestureStarted() - invoked when user starts a gesture. Return true if you want to show the grid. Return false if you want to hide the grid.
 - onGestureCompleted() - invoked when completes the gesture. Return true if you want to show the grid. Return false if you want to hide the grid.

## CropInfo

 ### What is it?
 CropInfo is a state of the crop which has data which can be used to crop the bitmap at a later stage. CropperImageView now uses
 CropInfo to crop the bitmap.

 This `state` of crop can be used on a scaled version of the same image/bitmap. If you have a large
 enough bitmap but you don't want to load it.
 You can load a scaled down version of it, when user is done cropping, get `CropInfo`,
 apply `CropInfo.scaleInfo(factor)` which gives a scaled version of CropInfo which can be used to crop the original bigger image with
 the same crop bounds as the user chose.

 It can also be projected for an *un-rotated* version of the bitmap. Eg. User rotates and crops the bitmap, but you want
 to crop the original (un-scaled) bitmap without having to rotate it. You can first *"un-rotate"* the CropInfo by using
 `CropInfo.rotate90XTimes(int w, int h, int times)` where times corresponds to the number of times the bitmap was rotated by 90 degrees
 clockwise. It will give you a projection of crop state which you can then scale and use to crop the original image.

 ### How to get it?

  `CropperView.getCropInfo()` will return `CropResult`. If cropping can be done, i.e. bitmap is loaded and user is not mid-gesture, CropResult
  will contain a valid value of `CropInfo`.

 ### Transformations

  - scaleFactor(float factor) - Crop info to be used for original un-scaled image. factor = `original width / scaled width`.
   Note: If your scaled down bitmap is rotated by 90 degrees, you would need to get the correct scale factor - which would be `original width / scaled height`.
   Check the sample for more details.

  - rotate90(int width) - Crop info to be used for original un-rotated image. `width - width of the rotated bitmap => height of the un-rotated bitmap`.
    I would advise you not to use this as it supports only if your bitmap is rotated by 90 degrees clockwise.

  - rotate90XTimes(int width, int height, int times) -  Crop info to be used for original un-rotated image. `width and height of the rotated bitmap`. `times - number of times the original bitmap was rotated by 90 degrees clockwise`.
   Check the sample for more details.

 ### How to use it?
 Once you have obtained CropInfo and transformed it the way you like, use `Cropper` to crop the original bitmap.
 ```
 Cropper cropper = new Cropper(cropInfo, originalBitmap);
 cropper.crop(callback) # for async
 # or
 cropper.cropBitmap() # for sync
 ```

 You can also use `ScaledCropper`. It will take care of scaling the cropInfo for you.
 ```
 ScaledCropper cropper = new ScaledCropper(cropInfo, originalBitmap, scaleFactor);
 cropper.crop(callback) # for async
 # or
 cropper.cropBitmap() # for sync
 ```

## CropMatrix

 ### What is it?

 CropMatrix is a state of the `CropperView` based on the translation and zoom of the image. This can be used if you are working with
 multiple crops or if the user paused the app and you would like to restore the exact position and zoom of the image in the cropper.

 ### How to get it?

 `CropperView.getCropMatrix()` will return `CropMatrix`. It contains `scale`, `xTranslation` and `yTranslation`. It's best if it's not edited manually as invalid values may lead to undesired behavior.

 ### How to use it?

 When you wish to restore the saved position, use `CropperView.setCropMatrix(matrix, animate)`. `animate` is a boolean flag. If it's true, the image would animate from the current position and scale to the new one.
 If `animate` is false, it will go to the new position and scale instantly.

 **Note**: If you want to call this method right after setting the Bitmap, you'd need to call it with delay (wait for the bitmap to load).

### 0.2 to 0.3 update note:

 - `CropInfo` has been introduced. You can use it crop original un-scaled or un-rotated bitmaps.
 - `CropMatrix` has been introduced. You can use it to restore the state of the image.


### 0.1 to 0.2 update note:

All the styleables are renamed to have prefix `nocropper__` so as not to have collision issues with other resource attributes. By collision I mean, your app will not
build if it has to resources attributes with same name.

## Licenses and Release History

**[CHANGELOG](https://github.com/jayrambhia/CropperNoCropper/blob/master/Changelog.md)**

NoCropper binaries and source code can be used according to the [Apache License, Version 2.0](https://github.com/jayrambhia/CropperNoCropper/blob/master/License).

