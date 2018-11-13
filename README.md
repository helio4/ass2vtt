# What is ass2vtt?
ass2vtt started as an .ass to .vtt converter that, unlike conventional converters, supports karaoke but it has evolved into a
subtitle converter specially aimed for captioning on YouTube.
# What fomat conversions does it support?
* .ass to .vtt (Basic lines and Karaoke)
* .vtt to .ass (Basic lines and Karaoke)
* .vtt to .ttml (Just for Karaoke)
* .ass to .xml (Basic lines, Karaoke, Styling and Positioning)
# How does the .ass to .xml conversion work?
### Styles
ass2vtt tries to make the conversion as close as possible to the original .ass file but, because of how Aegisub works, there
are a few things to keep in mind when styling on Aegisub's styles manager:
* The background color and opacity are defined by the "Shadow" color and alfa value.
* The edge color is defined by the color of the "Outline" and if the alfa value of the "Outline" is higher than 127 the .xml's edges will be disabled.
* To toggle on and off the background you have to check/uncheck the "Opaque box" checkbox.
* If the edges are disabled (Outline's alfa value > 127), the "Opaque box" is unchecked and the shadow's alfa value is lower than 255
then the edge type #1 (shadow) with the shadow's color will be applied.

I think these are the main "exceptions" to keep in mind.
### What is not supported?
Keep in mind you can still do some of these manually by editing the .xml file, but here's a list of what this conversion does not support (yet):
* Partial coloring/styling with .ass tags (eg. {\c}, {\i1}, etc.) 
* Aegisub's styles manager's Margins and Miscellaneous options.
* Strikeout/Strikethrough text.

I'll work as soon as possible in implementing the ones that are possible.
