## How to build action icons for the toolbar

* Open SVG image file ic_action_*.png with [Inkscape](http://inkscape.org/)
* Export as PNG (image size : 512px / 512px, page format)
* Use [Android Asset Studio](http://android-ui-utils.googlecode.com/hg/asset-studio/dist/icons-actionbar.html#source.space.trim=1&source.space.pad=0&name=example&theme=light)
* Upload the exported PNG image from image source, set a correct name (e.g. marker_light), apply Holo Light theme and download the zip file
* For each generated image file (as PNG) in different size, open [Gimp](http://www.gimp.org/)
* In Layer Dialog, set the opacity to 50 for the current layer
* Export the image as PNG and add "_disable" as suffix for the name
* Reset the opacity to 100 and use Colorize tool from menu Tools > Color Tools -> Colorize
* Apply the following settings : Hue : 200, Saturation : 100, Lightness : -40
* Export the image as PNG and add "_selected" as suffix for the name
