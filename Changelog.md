### V 0.3.2 (2021-05-01)

 - Migrated from Bintray to JitPack.
 - Using AndroidX in Sample.

### V 0.3.1 (2019-09-23)
 - `CropMatrix` added. Use this to preserve the state of scale and translation.

### V 0.3.0 (2017-12-30)
 - Pre-scaling support is here! It's just different now.
 - Load scaled down version of the original image and use `CropInfo`.

### V 0.2.1 (2017-12-27)
- Changes in getCroppedBitmap and getCroppedBitmapAsync method.
- Fixed issue where image was being fixed to an edge.
- Added GridCallback to control when you want to show the grid.

### V 0.2.0 (2017-05-03)
Option to load image as fit to center. Updated resource attributes to have prefix `nocropper__`. Fixed base zoom issue.

### V 0.1.9 (2017-04-20)
Fixed [image center panning issue with min zoom](https://github.com/jayrambhia/CropperNoCropper/issues/21). Fixed overriding of `min zoom` by the view.

### V 0.1.8 (2017-04-14)
Fixed [panning with min zoom](https://github.com/jayrambhia/CropperNoCropper/issues/10). Async cropping support added.

### V 0.1.7 (2016-11-18)
Optimized cropping using canvas.

### V 0.1.6 (2016-05-24)
Fixed a bug where cropped image was still based on the original image and not replaced image.

### V 0.1.5 (2016-05-24)
Option added for users to enable/disable gestures.
Option added for users to replace the bitmap (without changing the matrix)

### V 0.1.4 (2015-12-11) : 
Option added for the user to choose if they want to add padding to make the image square or not

### V 0.1.3 (2015-12-04) : 
Crop support added when the user is interacting with the image

### V 0.1.2 (2015-11-19) : 
Landscape mode support added

### V 0.1.1 (2015-11-13) : 
First Public Release