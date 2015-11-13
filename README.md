## Cropper - NoCropper

This is a lightweight Image Cropper for Android which also supports no-crop feature.

**[Project Page](http://www.jayrambhia.com/project/nocropper/)**
**[Blogpost](http://www.jayrambhia.com/blog/cropper/)**

Here's a short gif showing how it works.

![Demo](https://raw.githubusercontent.com/jayrambhia/CropperNoCropper/master/art/demo1.gif)

And, here's a bit longer [YouTube Video](https://youtu.be/OoYSt2vtdNs)

## CropperView

It's a FrameLayout which contains a view for Grid and an imageview. This project supports only square cropping.
CropperView contains some basic methods like `setImageBitmap()`, `setMaxZoom()`, `setMinZoom()`, etc which are
forwarded to `CropperImageView`.

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
        compile 'com.fenchtose.nocropper:nocropper:0.1.1'
    }

## CropperImageView

It's a square ImageView which acts as the cropper. It tries to keep the image in the range of max and min zoom.
It automatically adjusts the position of the image, if it's zoomed out.

### Useful Methods:

 - `setMaxZoom(float zoom)` set Maximum zoom
 - `setMinZoom(float zoom)` set Minimum zoom
 - `setImageBitmap(Bitmap bm)` set Bitmap
 - `cropToCenter()` - Set Image in the center with square crop view
 - `fitToCenter()` - Fit Image in the center (no cropping view)

### Styleables

 - `grid_color` - Color of the grid
 - `grid_thickness` - Thickness of grid lines
 - `grid_opacity` - Opacity of grid lines
 - `padding_color` - Color of the image padding

## Licenses and Release History

**[CHANGELOG](https://github.com/jayrambhia/CropperNoCropper/blob/master/Changelog.md)**

NoCropper binaries and source code can be used according to the [Apache License, Version 2.0](https://github.com/jayrambhia/CropperNoCropper/blob/master/License).

