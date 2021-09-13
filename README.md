# What is ass2vtt?
ass2vtt is the first multiformat subtitle converter exclusively designed for YouTube's caption system, allowing for some cool styling options. Here's an example of karaoke subtitles that make use of ass2vtt:<br/>
<p align="center">
  <img src="https://user-images.githubusercontent.com/24983230/133127255-50a7820d-2628-42a9-8f9f-44d96fc69cc9.png" alt="Yuni's Toumeiseisai Captions" width="75%">
  <br/>
  <a href="https://www.youtube.com/watch?v=BnH6RiFm9f4"><sub>From Yuni's Toumeiseisai Music Video (subtitles by Loserbait)</sub></a>
</p> 

# What format conversions does it support?
* .ass to .vtt (Basic lines and Karaoke)
* .vtt to .ass (Basic lines and Karaoke)
* .vtt to .ttml (Just for Karaoke)
* .ass to Format3 (.xml) (Basic lines, Karaoke, Styling and Positioning)

# How does the .ass to Format3 conversion work?
### Styles
ass2vtt tries to make the conversion as close as possible to the original .ass file but, because of how Aegisub works, there
are a few things to keep in mind when styling on Aegisub's styles manager:
* The background color and opacity are defined by the "Shadow" color and alfa value.
* The edge color is defined by the color of the "Outline" and if the alfa value of the "Outline" is higher than 127 the Format3's edges will be disabled.
* To toggle on or off the background you have to check/uncheck the "Opaque box" checkbox.
* If the edges are disabled (Outline's alfa value > 127), the "Opaque box" is unchecked and the shadow's alfa value is lower than 255
then the edge type #1 (shadow) with the shadow's color will be applied.

I think these are the main "exceptions" to keep in mind.
### What is not supported?
Keep in mind you can still do some of these manually by editing the .xml (Format3) file, but here's a list of what this conversion does not support:
* Partial coloring/styling with .ass tags (eg. {\c}, {\i1}, etc.) 
* Aegisub's styles manager's Margins and Miscellaneous options.
* Strikeout/Strikethrough text.

# ⚠Final note⚠
**This project is no longer being maintained, so if you are interested in Format3 (now also called YTT) conversion, I wholeheartedly encourage you to check arc's [YTSubConverter](https://github.com/arcusmaximus/YTSubConverter). Not only is being actively maintained but it's also richer in features.**
