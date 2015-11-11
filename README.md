## Cropper - NoCropper

This is a demo project which replicates Instagram's new cropper.

### CropperView

It's a FrameLayout which contains a view for Grid and an imageview. This project supports only square cropping.
CropperView contains some basic methods like `setImageBitmap()`, `setMaxZoom()`, `setMinZoom()`, etc which are
forwarded to `CropperImageView`.

### CropperImageView

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