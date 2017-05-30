## How to build action icons for the toolbar

### From a SVG image file

* Open SVG image file ic_action_*.png with [Inkscape](http://inkscape.org/)
* Export as PNG (image size : 512px / 512px, page format)
* Use [Android Asset Studio](http://android-ui-utils.googlecode.com/hg/asset-studio/dist/icons-actionbar.html#source.space.trim=1&source.space.pad=0&name=example&theme=light)
* Upload the exported PNG image from image source, set a correct name (e.g. marker_light), apply Holo Light theme and download the zip file
* For each generated image file (as PNG) in different size, open [Gimp](http://www.gimp.org/)
* In Layer Dialog, set the opacity to 50 for the current layer
* Export the image as PNG and add "_disable" as suffix for the name
* Reset the opacity to 100 and use Colorize tool from menu Tools > Color Tools -> Colorize
* Apply the following settings : Hue : 200, Saturation : 100, Lightness : 20
* Export the image as PNG and add "_selected" as suffix for the name

### From a GIMP image format (xcf)

* For the current layer, export as PNG (image size : 512px width or height) and add "_normal" as suffix for the name
* In Layer Dialog, set the opacity to 50 for the current layer
* Export the image as PNG and add "_disable" as suffix for the name
* Reset the opacity to 100 and use Colorize tool from menu Tools > Color Tools -> Colorize
* Apply the following settings : Hue : 200, Saturation : 100, Lightness : -40
* Export the image as PNG and add "_selected" as suffix for the name

## Launcher icon

From https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html

* **Foreground**: Image `ic_launcher_flora.svg`
* **Trim whitespace**: Trim
* **Padding**: 25%
* **Color**: white
* **Background color**: use `colorPrimary` from styles
* **Scaling**: Center
* **Shape**: Square
* **Effect**: Cast shadow
* **Name**: `ic_launcher`